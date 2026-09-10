package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.RecibirConversacionArchivada;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
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
 * Orquesta la ingesta completa: parsear, deduplicar contra lo ya guardado,
 * segmentar en casos, y disparar el analisis IA de cada caso nuevo.
 *
 * La empresa (tenant) ya viene resuelta en ContextoEmpresaActual -- la puso
 * el filtro del webhook a partir del secreto del header, antes de llegar
 * aca. Este servicio no ve el secreto.
 *
 * El payload crudo se pasa TAL CUAL (Map) al puerto -- convertirlo a JSON
 * es un detalle de como se guarda, no algo que le corresponda decidir a la
 * aplicacion. Quien lo serializa es el adaptador JPA (infraestructura), que
 * ya tiene Jackson a mano para eso.
 */
public class RecibirConversacionArchivadaService implements RecibirConversacionArchivada {

    private final RepositorioConversaciones repositorioConversaciones;
    private final RepositorioCasos repositorioCasos;
    private final AnalizadorDeConversacion analizadorDeConversacion;
    private final RepositorioAnalisisEscritor escritorAnalisis;

    public RecibirConversacionArchivadaService(RepositorioConversaciones repositorioConversaciones,
                                                RepositorioCasos repositorioCasos,
                                                AnalizadorDeConversacion analizadorDeConversacion,
                                                RepositorioAnalisisEscritor escritorAnalisis) {
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
        this.analizadorDeConversacion = analizadorDeConversacion;
        this.escritorAnalisis = escritorAnalisis;
    }

    @Override
    public void ejecutar(Map<String, Object> payload) {
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
            return;
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
                if (!FiltroDeRelevancia.esTurnoDeEncuesta(segmento.get(j).mensaje())) {
                    indicesRelevantes.add(j);
                }
            }
            if (indicesRelevantes.isEmpty()) {
                continue;
            }
            if (!FiltroDeRelevancia.segmentoTieneContenidoReal(segmento)) {
                continue;
            }

            int ultimoIndiceRelevante = indicesRelevantes.get(indicesRelevantes.size() - 1);
            int finRelativoRelevante = inicioRelativo + ultimoIndiceRelevante;

            Caso caso = repositorioCasos.crear(conversacion.id(), ordenInicial + inicioRelativo,
                    ordenInicial + finRelativoRelevante, esDeAds);
            casosCreados.add(caso);
        }

        for (Caso caso : casosCreados) {
            try {
                escritorAnalisis.analizarYGuardar(caso, analizadorDeConversacion, repositorioConversaciones,
                        repositorioCasos, idContacto);
            } catch (Exception ignored) {
                // El caso queda con procesada=false -- se reintenta despues via
                // GestionarReprocesamiento. No se propaga: un caso fallido no
                // debe tumbar la respuesta 200 al webhook de LIWA.
            }
        }
    }

    private List<TurnoParseado> filtrarTurnosNuevos(Long conversacionId, List<TurnoParseado> turnosDelPayload) {
        List<com.marcablanca.platform.omnicanal.domain.Turno> existentes =
                repositorioConversaciones.listarTurnos(conversacionId);
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
