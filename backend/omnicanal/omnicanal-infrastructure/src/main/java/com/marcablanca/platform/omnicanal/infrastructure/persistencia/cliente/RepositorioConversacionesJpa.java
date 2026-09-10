package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.Conversacion;
import com.marcablanca.platform.omnicanal.domain.Turno;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class RepositorioConversacionesJpa implements RepositorioConversaciones {

    private final ConversacionJpaRepository conversaciones;
    private final TurnoJpaRepository turnos;
    private final ObjectMapper json;

    RepositorioConversacionesJpa(ConversacionJpaRepository conversaciones, TurnoJpaRepository turnos, ObjectMapper json) {
        this.conversaciones = conversaciones;
        this.turnos = turnos;
        this.json = json;
    }

    /**
     * datos_crudos es NOT NULL y guarda el payload de LIWA para auditoria: si por
     * lo que sea no serializa, se guarda "{}" en vez de tumbar la ingesta.
     */
    private String aJson(Map<String, Object> valor) {
        try {
            return json.writeValueAsString(valor);
        } catch (JacksonException e) {
            return "{}";
        }
    }

    @Override
    public Optional<Conversacion> buscarPorIdContacto(String idContacto) {
        return conversaciones.findByIdContacto(idContacto).map(this::mapear);
    }

    @Override
    public Conversacion crear(String idContacto, String nombreContacto, String historialChatCompleto,
                               Map<String, Object> datosCrudos, boolean esDeAds) {
        var e = new ConversacionEntity(idContacto, nombreContacto, historialChatCompleto,
                aJson(datosCrudos), esDeAds);
        return mapear(conversaciones.save(e));
    }

    @Override
    public Conversacion actualizar(Long id, String historialChatCompleto, Map<String, Object> datosCrudos,
                                    boolean esDeAds, OffsetDateTime archivadaEn) {
        var e = conversaciones.findById(id).orElseThrow();
        e.actualizar(historialChatCompleto, aJson(datosCrudos), esDeAds, archivadaEn);
        return mapear(conversaciones.save(e));
    }

    @Override
    public void marcarSoloArchivado(Long id, boolean esDeAds) {
        var e = conversaciones.findById(id).orElseThrow();
        e.marcarSoloArchivado(esDeAds, OffsetDateTime.now());
        conversaciones.save(e);
    }

    @Override
    public List<Turno> listarTurnos(Long conversacionId) {
        return turnos.findByConversacionIdOrderByOrdenAsc(conversacionId).stream().map(this::mapearTurno).toList();
    }

    @Override
    public int siguienteOrden(Long conversacionId) {
        return turnos.siguienteOrden(conversacionId);
    }

    @Override
    public void guardarTurnos(Long conversacionId, int ordenInicial, List<TurnoParseadoConOrden> lista) {
        for (var t : lista) {
            turnos.save(new TurnoEntity(conversacionId, t.orden(), t.autor(), t.nombreAutor(), t.mensaje(), t.ocurridoEn()));
        }
    }

    @Override
    public Pagina<Conversacion> listarRecientes(String idContacto, int pagina, int porPagina, OffsetDateTime desde,
                                                 OffsetDateTime hasta) {
        var page = conversaciones.buscarRecientes(idContacto, desde, hasta, PageRequest.of(pagina - 1, porPagina));
        return new Pagina<>(page.getTotalElements(), pagina, porPagina, Math.max(1, page.getTotalPages()),
                page.getContent().stream().map(this::mapear).toList());
    }

    private Conversacion mapear(ConversacionEntity e) {
        return new Conversacion(e.getId(), e.getUuid(), e.getIdContacto(), e.getNombreContacto(),
                e.getHistorialChatCompleto(), e.getDatosCrudos(), e.isEsDeAds(), e.getCreadoEn(), e.getArchivadaEn());
    }

    private Turno mapearTurno(TurnoEntity e) {
        return new Turno(e.getId(), e.getUuid(), e.getConversacionId(), e.getOrden(), e.getAutor(),
                e.getNombreAutor(), e.getMensaje(), e.getOcurridoEn());
    }
}
