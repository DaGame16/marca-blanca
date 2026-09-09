package com.marcablanca.platform.consola.infrastructure.persistencia;

import com.marcablanca.platform.consola.application.port.out.RegistroDeAuditoria;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.UUID;

/** Inserta una fila por accion de operador en plataforma.tbl_auditoria_consola. */
@Component
class RegistroDeAuditoriaJdbc implements RegistroDeAuditoria {

    private final JdbcTemplate jdbc;

    RegistroDeAuditoriaJdbc(@Qualifier("controlDataSource") DataSource controlDataSource) {
        this.jdbc = new JdbcTemplate(controlDataSource);
    }

    @Override
    public void registrar(UUID operadorId, String accion, UUID empresaUuid) {
        jdbc.update("""
                insert into plataforma.tbl_auditoria_consola (operador_id, accion, empresa_uuid)
                values (?, ?, ?)""",
                operadorId, accion, empresaUuid);
    }
}
