package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.TurnoParseadoConOrden;
import com.marcablanca.platform.omnicanal.domain.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parsear, deduplicar contra lo ya guardado, persistir conversacion + turnos,
 * y segmentar en casos. La empresa (tenant) ya viene resuelta en
 * ContextoEmpresaActual -- la puso el filtro del webhook.
 *
 * El payload crudo se pasa TAL CUAL (Map) al puerto -- serializarlo a JSON es
 * detalle del adaptador JPA.
 */
public class IngestarConversacionArchivadaService implements IngestarConversacionArchivada {

    private final RepositorioConversaciones repositorioConversaciones;
    private final RepositorioCasos repositorioCasos;
    private final RepositorioConfiguracionOmnicanal configuracion;

    public IngestarConversacionArchivadaService(RepositorioConversaciones repositorioConversaciones,
                                                RepositorioCasos repositorioCasos,
                                                RepositorioConfiguracionOmnicanal configuracion) {
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
        this.configuracion = configuracion;
    }

    @Override
    public Ingesta ejecutar(Map<String, Object> payload) {
        FiltroDeRelevancia filtro = new FiltroDeRelevancia(configuracion.deLaEmpresaActiva().perfil());

        String idContacto = String.valueOf(payload.getOrDefault("user_id", "desconocido"));
        String historial = String.valueOf(payload.getOrDefault("chat_history_details_large", ""));
        boolean esDeAds = "1".equals(payload.get("ads"));
        String nombreContacto = payload.get("contact_name") != null ? payload.get("contact_name").toString() : null;

        var existente = repositorioConversaciones.buscarPorIdContacto(idContacto);
        List<TurnoParseado> turnosDelPayload = ParseadorDeTurnos.parsear(historial);
        List<TurnoParseado> turnosNuevos = existente.isPresent()
                ? filtrarTurnosNuevos(existente.get().id(), turnosDelPayload)
                : turnosDelPayload;

        if (existente.isPresent() && turnosNuevos.isEmpty()) {
            repositorioConversaciones.marcarSoloArchivado(existente.get().id(), esDeAds);
            return new Ingesta(idContacto, List.of());
        }

        Conversacion conversacion = existente.isPresent()
                ? repositorioConversaciones.actualizar(existente.get().id(), historial, payload, esDeAds,
                        OffsetDateTime.now())
                : repositorioConversaciones.crear(idContacto, nombreContacto, historial, payload, esDeAds);

        int ordenInicial = repositorioConversaciones.siguienteOrden(conversacion.id());
        if (!turnosNuevos.isEmpty()) {
            List<TurnoParseadoConOrden> paraGuardar = new ArrayList<>();
            for (int i = 0; i < turnosNuevos.size(); i++) {
                TurnoParseado t = turnosNuevos.get(i);
                paraGuardar.add(new TurnoParseadoConOrden(ordenInicial + i, t.autor(), t.nombre(), t.mensaje(), t.fecha()));
            }
            repositorioConversaciones.guardarTurnos(conversacion.id(), ordenInicial, paraGuardar);
        }

        List<Integer> cortes = turnosNuevos.isEmpty() ? List.of() : DetectorDeCortes.detectarCortes(turnosNuevos);
        List<Caso> casosCreados = new ArrayList<>();

        for (int i = 0; i < cortes.size(); i++) {
            int inicioRelativo = cortes.get(i);
            int finRelativo = (i + 1 < cortes.size() ? cortes.get(i + 1) - 1 : turnosNuevos.size() - 1);
            List<TurnoParseado> segmento = turnosNuevos.subList(inicioRelativo, finRelativo + 1);

            List<Integer> indicesRelevantes = new ArrayList<>();
            for (int j = 0; j < segmento.size(); j++) {
                if (!filtro.esTurnoDeEncuesta(segmento.get(j).mensaje())) {
                    indicesRelevantes.add(j);
                }
            }
            if (indicesRelevantes.isEmpty()) {
                continue;
            }
            if (!filtro.segmentoTieneContenidoReal(segmento)) {
                continue;
            }

            int ultimoIndiceRelevante = indicesRelevantes.get(indicesRelevantes.size() - 1);
            int finRelativoRelevante = inicioRelativo + ultimoIndiceRelevante;

            Caso caso = repositorioCasos.crear(conversacion.id(), ordenInicial + inicioRelativo,
                    ordenInicial + finRelativoRelevante, esDeAds);
            casosCreados.add(caso);
        }

        return new Ingesta(idContacto, casosCreados);
    }

    private List<TurnoParseado> filtrarTurnosNuevos(Long conversacionId, List<TurnoParseado> turnosDelPayload) {
        List<Turno> existentes = repositorioConversaciones.listarTurnos(conversacionId);
        Set<String> firmasExistentes = new HashSet<>();
        for (var t : existentes) {
            firmasExistentes.add(firma(t.nombreAutor(), t.ocurridoEn(), t.mensaje()));
        }
        return turnosDelPayload.stream()
                .filter(t -> !firmasExistentes.contains(firma(t.nombre(), t.fecha(), t.mensaje())))
                .toList();
    }

    private String firma(String nombre, OffsetDateTime fecha, String mensaje) {
        return (nombre == null ? "" : nombre) + "|" + (fecha == null ? "" : fecha) + "|" + mensaje;
    }
}
