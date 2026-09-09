package com.marcablanca.platform.consola.infrastructure.persistencia;

import com.marcablanca.platform.consola.application.port.out.RepositorioOperadores;
import com.marcablanca.platform.consola.domain.Operador;
import com.marcablanca.platform.consola.domain.RolOperador;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Operadores en {@code plataforma.tbl_operadores}, en la base de control. Se usa
 * {@code JdbcTemplate} directo (no JPA) porque es una tabla chica y sigue el
 * mismo patron que el pipeline de aprovisionamiento para la base de control.
 */
@Component
public class RepositorioOperadoresJdbc implements RepositorioOperadores {

    private static final String COLUMNAS =
            "id, correo, nombre, hash_contrasena, rol, activo, es_contrasena_temporal";

    private final JdbcTemplate jdbc;

    public RepositorioOperadoresJdbc(@Qualifier("controlDataSource") DataSource controlDataSource) {
        this.jdbc = new JdbcTemplate(controlDataSource);
    }

    @Override
    public Optional<Operador> buscarPorCorreo(String correo) {
        return uno("select " + COLUMNAS + " from plataforma.tbl_operadores where correo = ?", correo);
    }

    @Override
    public Optional<Operador> buscarPorId(UUID id) {
        return uno("select " + COLUMNAS + " from plataforma.tbl_operadores where id = ?", id);
    }

    @Override
    public void guardar(Operador operador) {
        int filas = jdbc.update("""
                update plataforma.tbl_operadores
                set nombre = ?, hash_contrasena = ?, rol = ?, activo = ?,
                    es_contrasena_temporal = ?, actualizado_en = now()
                where id = ?""",
                operador.getNombre(), operador.getHashContrasena(), operador.getRol().name().toLowerCase(),
                operador.estaActivo(), operador.debeCambiarContrasena(), operador.getId());

        if (filas == 0) {
            jdbc.update("""
                    insert into plataforma.tbl_operadores
                        (id, correo, nombre, hash_contrasena, rol, activo, es_contrasena_temporal)
                    values (?, ?, ?, ?, ?, ?, ?)""",
                    operador.getId(), operador.getCorreo(), operador.getNombre(), operador.getHashContrasena(),
                    operador.getRol().name().toLowerCase(), operador.estaActivo(), operador.debeCambiarContrasena());
        }
    }

    private Optional<Operador> uno(String sql, Object argumento) {
        List<Operador> filas = jdbc.query(sql, this::mapear, argumento);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }

    private Operador mapear(ResultSet rs, int numeroFila) throws SQLException {
        return new Operador(
                rs.getObject("id", UUID.class),
                rs.getString("correo"),
                rs.getString("nombre"),
                rs.getString("hash_contrasena"),
                RolOperador.valueOf(rs.getString("rol").toUpperCase()),
                rs.getBoolean("activo"),
                rs.getBoolean("es_contrasena_temporal"));
    }
}
