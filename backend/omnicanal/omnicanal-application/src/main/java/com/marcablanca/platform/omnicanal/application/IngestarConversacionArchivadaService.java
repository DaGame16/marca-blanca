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
import java.util.Optional;
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
        DatosPayload datos = DatosPayload.de(payload);

        Optional<Conversacion> existente = repositorioConversaciones.buscarPorIdContacto(datos.idContacto());
        List<TurnoParseado> turnosDelPayload = ParseadorDeTurnos.parsear(datos.historial());
        List<TurnoParseado> turnosNuevos = existente
                .map(c -> filtrarTurnosNuevos(c.id(), turnosDelPayload))
                .orElse(turnosDelPayload);

        if (existente.isPresent() && turnosNuevos.isEmpty()) {
            repositorioConversaciones.marcarSoloArchivado(existente.get().id(), datos.esDeAds());
            return new Ingesta(datos.idContacto(), List.of());
        }

        Conversacion conversacion = persistirConversacion(existente, datos, payload);
        int ordenInicial = repositorioConversaciones.siguienteOrden(conversacion.id());
        persistirTurnos(conversacion.id(), ordenInicial, turnosNuevos);

        List<Caso> casosCreados = segmentarEnCasos(filtro, turnosNuevos, conversacion.id(), ordenInicial,
                datos.esDeAds());
        return new Ingesta(datos.idContacto(), casosCreados);
    }

    private Conversacion persistirConversacion(Optional<Conversacion> existente, DatosPayload datos,
                                               Map<String, Object> payload) {
        return existente.isPresent()
                ? repositorioConversaciones.actualizar(existente.get().id(), datos.historial(), payload, datos.esDeAds(),
                        OffsetDateTime.now())
                : repositorioConversaciones.crear(datos.idContacto(), datos.nombreContacto(), datos.historial(), payload,
                        datos.esDeAds());
    }

    private List<Caso> segmentarEnCasos(FiltroDeRelevancia filtro, List<TurnoParseado> turnosNuevos, Long conversacionId,
                                        int ordenInicial, boolean esDeAds) {
        List<Integer> cortes = turnosNuevos.isEmpty() ? List.of() : DetectorDeCortes.detectarCortes(turnosNuevos);
        List<Caso> casosCreados = new ArrayList<>();
        for (int i = 0; i < cortes.size(); i++) {
            casoDelCorte(filtro, turnosNuevos, cortes, i, conversacionId, ordenInicial, esDeAds)
                    .ifPresent(casosCreados::add);
        }
        return casosCreados;
    }

    /** Campos del payload de LIWA que usa la ingesta, ya con sus defaults aplicados. */
    private record DatosPayload(String idContacto, String historial, boolean esDeAds, String nombreContacto) {
        static DatosPayload de(Map<String, Object> payload) {
            Object contactName = payload.get("contact_name");
            return new DatosPayload(
                    String.valueOf(payload.getOrDefault("user_id", "desconocido")),
                    String.valueOf(payload.getOrDefault("chat_history_details_large", "")),
                    "1".equals(payload.get("ads")),
                    contactName != null ? contactName.toString() : null);
        }
    }

    private void persistirTurnos(Long conversacionId, int ordenInicial, List<TurnoParseado> turnosNuevos) {
        if (turnosNuevos.isEmpty()) {
            return;
        }
        List<TurnoParseadoConOrden> paraGuardar = new ArrayList<>();
        for (int i = 0; i < turnosNuevos.size(); i++) {
            TurnoParseado t = turnosNuevos.get(i);
            paraGuardar.add(new TurnoParseadoConOrden(ordenInicial + i, t.autor(), t.nombre(), t.mensaje(), t.fecha()));
        }
        repositorioConversaciones.guardarTurnos(conversacionId, ordenInicial, paraGuardar);
    }

    /**
     * Un segmento (entre dos cortes) se vuelve caso solo si tiene contenido
     * real; si la encuesta quedo al final, se recorta el rango del caso.
     */
    private Optional<Caso> casoDelCorte(FiltroDeRelevancia filtro, List<TurnoParseado> turnosNuevos,
                                        List<Integer> cortes, int i, Long conversacionId, int ordenInicial,
                                        boolean esDeAds) {
        int inicioRelativo = cortes.get(i);
        int finRelativo = (i + 1 < cortes.size() ? cortes.get(i + 1) - 1 : turnosNuevos.size() - 1);
        List<TurnoParseado> segmento = turnosNuevos.subList(inicioRelativo, finRelativo + 1);

        int ultimoRelevante = -1;
        for (int j = 0; j < segmento.size(); j++) {
            if (!filtro.esTurnoDeEncuesta(segmento.get(j).mensaje())) {
                ultimoRelevante = j;
            }
        }
        if (ultimoRelevante < 0 || !filtro.segmentoTieneContenidoReal(segmento)) {
            return Optional.empty();
        }

        int finRelativoRelevante = inicioRelativo + ultimoRelevante;
        return Optional.of(repositorioCasos.crear(conversacionId, ordenInicial + inicioRelativo,
                ordenInicial + finRelativoRelevante, esDeAds));
    }

    private List<TurnoParseado> filtrarTurnosNuevos(Long conversacionId, List<TurnoParseado> turnosDelPayload) {
        List<Turno> existentes = repositorioConversaciones.listarTurnos(conversacionId);
        Set<String> firmasExistentes = new HashSet<>();
        for (Turno t : existentes) {
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
