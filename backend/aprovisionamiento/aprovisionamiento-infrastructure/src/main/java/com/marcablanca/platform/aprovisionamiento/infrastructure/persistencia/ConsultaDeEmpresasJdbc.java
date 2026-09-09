package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.application.DatosYMarcaDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ConsultaDeEmpresas;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lectura del catalogo de empresas para la consola. Un LEFT JOIN a
 * tbl_aprovisionamiento_tareas trae el paso y estado del pipeline si existe
 * (null si la empresa nunca lo disparo).
 */
@Component
class ConsultaDeEmpresasJdbc implements ConsultaDeEmpresas {

    private static final String SQL = """
            select e.uuid, e.identificador, e.nombre_legal, e.dominio, e.correo_contacto,
                   e.estado, e.creado_en, t.paso_actual, t.estado as estado_tarea
            from plataforma.tbl_empresas e
            left join plataforma.tbl_aprovisionamiento_tareas t on t.empresa_id = e.id
            order by e.creado_en desc""";

    private final JdbcTemplate jdbc;

    ConsultaDeEmpresasJdbc(@Qualifier("controlDataSource") DataSource controlDataSource) {
        this.jdbc = new JdbcTemplate(controlDataSource);
    }

    private static final String SQL_DETALLE = """
            select e.uuid, e.identificador, e.nombre_legal, e.dominio, e.representante_legal,
                   e.correo_contacto, e.telefono, e.sitio_web, e.estado,
                   m.color_primario, m.color_secundario, m.url_logo,
                   coalesce(m.tipo_login, 1) as tipo_login,
                   coalesce(m.tipo_pantalla_principal, 1) as tipo_pantalla_principal
            from plataforma.tbl_empresas e
            left join plataforma.tbl_empresas_marca m on m.empresa_id = e.id
            where e.uuid = ?""";

    @Override
    public List<ResumenDeEmpresa> listarTodas() {
        return jdbc.query(SQL, ConsultaDeEmpresasJdbc::mapear);
    }

    @Override
    public Optional<DatosYMarcaDeEmpresa> datosYMarca(UUID empresaId) {
        List<DatosYMarcaDeEmpresa> filas = jdbc.query(SQL_DETALLE, ConsultaDeEmpresasJdbc::mapearDetalle, empresaId);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }

    private static ResumenDeEmpresa mapear(ResultSet rs, int numeroFila) throws SQLException {
        OffsetDateTime creado = rs.getObject("creado_en", OffsetDateTime.class);
        return new ResumenDeEmpresa(
                rs.getObject("uuid", UUID.class),
                rs.getString("identificador"),
                rs.getString("nombre_legal"),
                rs.getString("dominio"),
                rs.getString("correo_contacto"),
                rs.getString("estado"),
                rs.getString("paso_actual"),
                rs.getString("estado_tarea"),
                creado == null ? null : creado.toInstant());
    }

    private static DatosYMarcaDeEmpresa mapearDetalle(ResultSet rs, int numeroFila) throws SQLException {
        return new DatosYMarcaDeEmpresa(
                rs.getObject("uuid", UUID.class),
                rs.getString("identificador"),
                rs.getString("nombre_legal"),
                rs.getString("dominio"),
                rs.getString("representante_legal"),
                rs.getString("correo_contacto"),
                rs.getString("telefono"),
                rs.getString("sitio_web"),
                rs.getString("estado"),
                rs.getString("color_primario"),
                rs.getString("color_secundario"),
                rs.getString("url_logo"),
                rs.getInt("tipo_login"),
                rs.getInt("tipo_pantalla_principal"));
    }
}
