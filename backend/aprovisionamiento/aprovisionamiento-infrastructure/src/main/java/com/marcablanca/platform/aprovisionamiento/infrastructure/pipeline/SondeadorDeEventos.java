package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.in.EjecutarAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaRegistrada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Sondea el outbox y dispara el pipeline por cada evento pendiente. Reclama filas
 * con UPDATE ... FOR UPDATE SKIP LOCKED para que dos instancias no procesen el
 * mismo evento. El procesamiento va FUERA de transaccion porque el pipeline hace
 * CREATE DATABASE.
 */
@Component
class SondeadorDeEventos {

    private static final Logger log = LoggerFactory.getLogger(SondeadorDeEventos.class);
    private static final int LOTE = 10;
    private static final long BACKOFF_TOPE_SEGUNDOS = 300;
    // Debe coincidir con el tipo_evento que escribe RegistroDeEventosJpaAdapter.
    private static final String TIPO_EMPRESA_REGISTRADA = "empresa.aprovisionamiento_solicitado";

    private final JdbcTemplate control;
    private final EjecutarAprovisionamiento ejecutarAprovisionamiento;
    private final ObjectMapper json;
    private final String worker = "sondeador-" + UUID.randomUUID();

    SondeadorDeEventos(@Qualifier("controlDataSource") DataSource controlDataSource,
                       EjecutarAprovisionamiento ejecutarAprovisionamiento,
                       ObjectMapper json) {
        this.control = new JdbcTemplate(controlDataSource);
        this.ejecutarAprovisionamiento = ejecutarAprovisionamiento;
        this.json = json;
    }

    @Scheduled(fixedDelayString = "${app.aprovisionamiento.sondeo-ms:5000}")
    public void sondear() {
        for (FilaOutbox fila : reclamar()) {
            procesar(fila);
        }
    }

    private List<FilaOutbox> reclamar() {
        return control.query(
                """
                update plataforma.tbl_eventos_salientes
                   set estado = 'procesando', bloqueado_en = now(), bloqueado_por = ?, actualizado_en = now()
                 where id in (
                       select id from plataforma.tbl_eventos_salientes
                        where estado = 'pendiente' and disponible_en <= now()
                        order by id
                        for update skip locked
                        limit ?)
                returning id, tipo_evento, payload, intentos, max_intentos
                """,
                (rs, n) -> new FilaOutbox(
                        rs.getLong("id"), rs.getString("tipo_evento"),
                        rs.getString("payload"), rs.getInt("intentos"), rs.getInt("max_intentos")),
                worker, LOTE);
    }

    private void procesar(FilaOutbox fila) {
        try {
            if (TIPO_EMPRESA_REGISTRADA.equals(fila.tipoEvento())) {
                ejecutarAprovisionamiento.ejecutar(deserializar(fila.payload()));
            } else {
                log.warn("Tipo de evento no reconocido en el outbox: {}", fila.tipoEvento());
            }
            control.update(
                    "update plataforma.tbl_eventos_salientes set estado = 'procesado', "
                            + "procesado_en = now(), actualizado_en = now() where id = ?",
                    fila.id());
        } catch (RuntimeException e) {
            int intentos = fila.intentos() + 1;
            boolean agotado = intentos >= fila.maxIntentos();
            long espera = Math.min(BACKOFF_TOPE_SEGUNDOS, (long) Math.pow(2, intentos) * 10);
            log.warn("Fallo el evento {} (intento {}/{}): {}",
                    fila.id(), intentos, fila.maxIntentos(), e.getMessage());
            control.update(
                    "update plataforma.tbl_eventos_salientes set estado = ?, intentos = ?, ultimo_error = ?, "
                            + "disponible_en = now() + make_interval(secs => ?), "
                            + "bloqueado_en = null, bloqueado_por = null, actualizado_en = now() where id = ?",
                    agotado ? "fallido" : "pendiente", intentos, e.getMessage(), (double) espera, fila.id());
        }
    }

    private EmpresaRegistrada deserializar(String payload) {
        JsonNode n = json.readTree(payload);
        Set<String> modulos = new LinkedHashSet<>();
        JsonNode arr = n.get("modulosSolicitados");
        if (arr != null) {
            arr.forEach(m -> modulos.add(m.asString()));
        }
        return new EmpresaRegistrada(
                UUID.fromString(n.get("empresaUuid").asString()),
                n.get("identificador").asString(),
                n.get("nombreLegal").asString(),
                textoONull(n, "nombreComercial"),
                textoONull(n, "dominio"),
                modulos,
                Instant.parse(n.get("ocurridoEn").asString()));
    }

    private static String textoONull(JsonNode n, String campo) {
        JsonNode f = n.get(campo);
        return (f == null || f.isNull() || f.asString().isBlank()) ? null : f.asString();
    }

    private record FilaOutbox(long id, String tipoEvento, String payload, int intentos, int maxIntentos) {
    }
}