package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.out.PasosDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Set;

/**
 * Unica pieza que ejecuta DDL. crearBaseDeDatos usa la conexion de mantenimiento
 * (owner @ 'postgres', autocommit). Los pasos que escriben en la base de control
 * usan su datasource. Cada metodo es idempotente: comprueba antes de actuar, para
 * poder reanudar el pipeline desde cualquier checkpoint sin duplicar efectos.
 */
@Component
class EjecutorDdlPostgres implements PasosDeAprovisionamiento {

    private static final Logger log = LoggerFactory.getLogger(EjecutorDdlPostgres.class);

    private final JdbcTemplate mantenimiento;
    private final JdbcTemplate control;
    private final String plantilla;
    private final String hostCliente;
    private final int puertoCliente;

    EjecutorDdlPostgres(DataSource dataSourceMantenimiento,
                        @Qualifier("controlDataSource") DataSource controlDataSource,
                        @Value("${app.aprovisionamiento.plantilla:db_plantilla_maestra}") String plantilla,
                        @Value("${app.aprovisionamiento.cliente-host:localhost}") String hostCliente,
                        @Value("${app.aprovisionamiento.cliente-puerto:5432}") int puertoCliente) {
        this.mantenimiento = new JdbcTemplate(dataSourceMantenimiento);
        this.control = new JdbcTemplate(controlDataSource);
        this.plantilla = plantilla;
        this.hostCliente = hostCliente;
        this.puertoCliente = puertoCliente;
    }

    @Override
    public void crearBaseDeDatos(Empresa empresa) {
        String nombreBd = empresa.getIdentificador().nombreBaseDeDatos();
        if (!nombreBd.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalStateException("Nombre de base no seguro para DDL: " + nombreBd);
        }
        Integer existe = mantenimiento.queryForObject(
                "select count(*) from pg_database where datname = ?", Integer.class, nombreBd);
        if (existe != null && existe > 0) {
            log.info("La base {} ya existe; no se clona de nuevo.", nombreBd);
            return;
        }
        log.info("Clonando {} desde la plantilla {}", nombreBd, plantilla);
        mantenimiento.execute("create database " + nombreBd + " template " + plantilla);
    }

    @Override
    public void aplicarSemilla(Empresa empresa) {
        // TODO (bloque 6b): dentro de db_cliente_<slug>, sembrar el usuario maestro
        // (usando hash_contrasena_maestra) y la marca. Necesita conexion a la base nueva.
        log.info("aplicarSemilla pendiente para {}", empresa.getIdentificador().valor());
    }

    @Override
    public void crearRolesDeTenant(Empresa empresa) {
        // TODO (bloque 6b): CREATE ROLE cli_<slug>_app / _lectura + guardar el secreto.
        // En DEV el enrutamiento usa el rol compartido guajiranet_app, no bloquea.
        log.info("crearRolesDeTenant pendiente para {}", empresa.getIdentificador().valor());
    }

    @Override
    public void registrarConexion(Empresa empresa) {
        String nombreBd = empresa.getIdentificador().nombreBaseDeDatos();
        Integer existe = control.queryForObject(
                "select count(*) from plataforma.tbl_empresa_conexiones c "
                        + "join plataforma.tbl_empresas e on e.id = c.empresa_id where e.uuid = ?",
                Integer.class, empresa.getId());
        if (existe != null && existe > 0) {
            return;
        }
        control.update(
                "insert into plataforma.tbl_empresa_conexiones (empresa_id, host, puerto, nombre_bd, es_activa) "
                        + "select e.id, ?, ?, ?, true from plataforma.tbl_empresas e where e.uuid = ?",
                hostCliente, puertoCliente, nombreBd, empresa.getId());
    }

    @Override
    public void registrarVersionDeEsquema(Empresa empresa) {
        Integer existe = control.queryForObject(
                "select count(*) from plataforma.tbl_empresa_esquema_version v "
                        + "join plataforma.tbl_empresas e on e.id = v.empresa_id where e.uuid = ?",
                Integer.class, empresa.getId());
        if (existe != null && existe > 0) {
            return;
        }
        // TODO (bloque 6b): leer el ultimo id de databasechangelog de la base recien clonada.
        control.update(
                "insert into plataforma.tbl_empresa_esquema_version (empresa_id, ultima_migracion_aplicada) "
                        + "select e.id, ? from plataforma.tbl_empresas e where e.uuid = ?",
                "clonada-de-plantilla", empresa.getId());
    }

    @Override
    public void poblarModulos(Empresa empresa, Set<String> modulosSolicitados) {
        // TODO (bloque 7): puente ACL hacia el puerto de entrada de modulos-empresa.
        log.info("poblarModulos pendiente ({} modulos) para {}",
                modulosSolicitados.size(), empresa.getIdentificador().valor());
    }
}