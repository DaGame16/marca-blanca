package com.marcablanca.platform.consola.infrastructure.arranque;

import com.marcablanca.platform.consola.application.port.out.CifradorDeContrasenaDeOperador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * El changeset 0020 siembra el primer SUPER_ADMIN con {@code hash_contrasena =
 * 'PENDIENTE'} (sentinela que nunca valida). Al arrancar, este componente le fija
 * la contrasena real:
 * <ul>
 *   <li>si esta la variable de entorno {@code CONSOLA_SUPERADMIN_PASSWORD}, la usa;</li>
 *   <li>si no, genera una temporal y la escribe en el log (WARN) para el primer ingreso.</li>
 * </ul>
 * En ambos casos {@code es_contrasena_temporal} sigue en true: se pedira cambiarla.
 * Idempotente: una vez que el hash deja de ser 'PENDIENTE', no vuelve a tocar nada.
 */
@Component
public class SembradorDeOperadorInicial {

    private static final Logger log = LoggerFactory.getLogger(SembradorDeOperadorInicial.class);
    private static final String HASH_PENDIENTE = "PENDIENTE";
    private static final char[] ALFABETO =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".toCharArray();
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final CifradorDeContrasenaDeOperador cifrador;
    private final String claveConfigurada;

    public SembradorDeOperadorInicial(@Qualifier("controlDataSource") DataSource controlDataSource,
                                      CifradorDeContrasenaDeOperador cifrador,
                                      @Value("${CONSOLA_SUPERADMIN_PASSWORD:}") String claveConfigurada) {
        this.jdbc = new JdbcTemplate(controlDataSource);
        this.cifrador = cifrador;
        this.claveConfigurada = claveConfigurada;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fijarContrasenaInicial() {
        List<Map<String, Object>> pendientes = jdbc.queryForList(
                "select id, correo from plataforma.tbl_operadores where hash_contrasena = ?", HASH_PENDIENTE);

        for (Map<String, Object> fila : pendientes) {
            boolean generada = claveConfigurada == null || claveConfigurada.isBlank();
            String clavePlana = generada ? generar() : claveConfigurada;

            jdbc.update("update plataforma.tbl_operadores set hash_contrasena = ?, actualizado_en = now() where id = ?",
                    cifrador.cifrar(clavePlana), fila.get("id"));

            if (generada) {
                log.warn("Operador inicial '{}' sin CONSOLA_SUPERADMIN_PASSWORD. "
                                + "Contrasena temporal generada: {}  (se pedira cambiarla en el primer ingreso)",
                        fila.get("correo"), clavePlana);
            } else {
                log.info("Operador inicial '{}': contrasena fijada desde CONSOLA_SUPERADMIN_PASSWORD; "
                        + "se pedira cambiarla en el primer ingreso.", fila.get("correo"));
            }
        }
    }

    private static String generar() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            sb.append(ALFABETO[ALEATORIO.nextInt(ALFABETO.length)]);
        }
        return sb.toString();
    }
}
