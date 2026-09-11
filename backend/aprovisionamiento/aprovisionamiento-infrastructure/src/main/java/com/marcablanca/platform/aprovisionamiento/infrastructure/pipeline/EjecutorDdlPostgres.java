package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.PasosDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeBienvenida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

/**
 * Unica pieza que ejecuta DDL. crearBaseDeDatos y crearRolesDeTenant usan la
 * conexion de mantenimiento (owner @ 'postgres', autocommit). aplicarSemilla,
 * registrarVersionDeEsquema y enviarBienvenida abren una conexion a la base del
 * cliente. registrarConexion escribe en la base de control. poblarModulos delega
 * en modulos-empresa via un puerto ACL.
 *
 * Cada metodo es idempotente: comprueba antes de actuar, para poder reanudar el
 * pipeline desde cualquier checkpoint sin duplicar efectos.
 */
@Component
class EjecutorDdlPostgres implements PasosDeAprovisionamiento {

    private static final Logger log = LoggerFactory.getLogger(EjecutorDdlPostgres.class);
    private static final String SLUG_SEGURO = "[a-z][a-z0-9_]*";
    private static final char[] ALFABETO_CONTRASENA =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".toCharArray();
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final JdbcTemplate mantenimiento;
    private final JdbcTemplate control;
    private final ActivadorDeModulosDeEmpresa activadorModulos;
    private final EnviarCorreoDeBienvenida enviarCorreoDeBienvenida;
    private final BCryptPasswordEncoder cifrador = new BCryptPasswordEncoder();
    private final String plantilla;
    private final String hostCliente;
    private final int puertoCliente;
    private final String ownerUsuario;
    private final String ownerClave;

    EjecutorDdlPostgres(DataSource dataSourceMantenimiento,
                        @Qualifier("controlDataSource") DataSource controlDataSource,
                        ActivadorDeModulosDeEmpresa activadorModulos,
                        EnviarCorreoDeBienvenida enviarCorreoDeBienvenida,
                        @Value("${app.aprovisionamiento.plantilla:db_plantilla_maestra}") String plantilla,
                        @Value("${app.aprovisionamiento.cliente-host:localhost}") String hostCliente,
                        @Value("${app.aprovisionamiento.cliente-puerto:5432}") int puertoCliente,
                        @Value("${app.aprovisionamiento.mantenimiento.username:guajiranet_owner}") String ownerUsuario,
                        @Value("${app.aprovisionamiento.mantenimiento.password:guajiranet_owner}") String ownerClave) {
        this.mantenimiento = new JdbcTemplate(dataSourceMantenimiento);
        this.control = new JdbcTemplate(controlDataSource);
        this.activadorModulos = activadorModulos;
        this.enviarCorreoDeBienvenida = enviarCorreoDeBienvenida;
        this.plantilla = plantilla;
        this.hostCliente = hostCliente;
        this.puertoCliente = puertoCliente;
        this.ownerUsuario = ownerUsuario;
        this.ownerClave = ownerClave;
    }

    @Override
    public void crearBaseDeDatos(Empresa empresa) {
        String nombreBd = empresa.getIdentificador().nombreBaseDeDatos();
        exigirNombreSeguro(nombreBd);
        if (existe(mantenimiento, "select exists(select 1 from pg_database where datname = ?)", nombreBd)) {
            log.info("La base {} ya existe; no se clona de nuevo.", nombreBd);
            return;
        }
        // CREATE DATABASE no admite parametros para los identificadores. Ambos nombres
        // se validan contra SLUG_SEGURO antes de interpolarse: nombreBd deriva del
        // Identificador (ya validado en el dominio) y plantilla es config de confianza.
        exigirNombreSeguro(plantilla);
        log.info("Clonando {} desde la plantilla {}", nombreBd, plantilla);
        mantenimiento.execute("create database " + nombreBd + " template " + plantilla);
    }

    @Override
    public void aplicarSemilla(Empresa empresa) {
        // El usuario admin se crea en enviarBienvenida (con la contrasena temporal).
        // Aca solo el rol ADMIN, idempotente.
        JdbcTemplate cliente = jdbcCliente(empresa.getIdentificador().nombreBaseDeDatos());
        if (!existe(cliente, "select exists(select 1 from seguridad.tbl_roles where nombre = 'ADMIN')")) {
            cliente.update("""
                    insert into seguridad.tbl_roles (nombre, descripcion, es_del_sistema)
                    values ('ADMIN', 'Administrador de la empresa', true)""");
        }
    }

    @Override
    public void crearRolesDeTenant(Empresa empresa) {
        String slug = empresa.getIdentificador().valor();
        exigirNombreSeguro(slug);
        crearRolSiFalta("cli_" + slug + "_app", "guajiranet_app");
        crearRolSiFalta("cli_" + slug + "_lectura", "guajiranet_lectura");
    }

    @Override
    public void registrarConexion(Empresa empresa) {
        String nombreBd = empresa.getIdentificador().nombreBaseDeDatos();
        boolean yaRegistrada = existe(control, """
                select exists(
                    select 1 from plataforma.tbl_empresa_conexiones c
                    join plataforma.tbl_empresas e on e.id = c.empresa_id
                    where e.uuid = ?)""", empresa.getId());
        if (yaRegistrada) {
            return;
        }
        control.update("""
                insert into plataforma.tbl_empresa_conexiones
                    (empresa_id, host, puerto, nombre_bd, secreto_ref, es_activa)
                select e.id, ?, ?, ?, 'dev-local', true
                from plataforma.tbl_empresas e where e.uuid = ?""",
                hostCliente, puertoCliente, nombreBd, empresa.getId());
    }

    @Override
    public void registrarVersionDeEsquema(Empresa empresa) {
        boolean yaRegistrada = existe(control, """
                select exists(
                    select 1 from plataforma.tbl_empresa_esquema_version v
                    join plataforma.tbl_empresas e on e.id = v.empresa_id
                    where e.uuid = ?)""", empresa.getId());
        if (yaRegistrada) {
            return;
        }
        control.update("""
                insert into plataforma.tbl_empresa_esquema_version (empresa_id, ultima_migracion_aplicada)
                select e.id, ? from plataforma.tbl_empresas e where e.uuid = ?""",
                ultimaMigracionDe(empresa.getIdentificador().nombreBaseDeDatos()), empresa.getId());
    }

    @Override
    public void poblarModulos(Empresa empresa, Set<String> modulosSolicitados) {
        for (String codigo : modulosSolicitados) {
            try {
                activadorModulos.activar(empresa.getId(), codigo);
                log.info("Modulo '{}' activado para {}", codigo, empresa.getIdentificador().valor());
            } catch (RuntimeException e) {
                log.warn("No se pudo activar el modulo '{}' para {}: {}",
                        codigo, empresa.getIdentificador().valor(), e.getMessage());
            }
        }
    }

    @Override
    public void enviarBienvenida(Empresa empresa) {
        boolean yaEnviada = existe(control, """
                select exists(
                    select 1 from plataforma.tbl_empresas
                    where uuid = ? and bienvenida_enviada_en is not null)""", empresa.getId());
        if (yaEnviada) {
            return;
        }

        String correo = empresa.getCorreo();
        String contrasenaTemporal = generarContrasenaTemporal();
        // TEMPORAL -- SOLO PARA PRUEBAS: sin SMTP funcionando (Render bloquea
        // salida SMTP en el plan free) esta es la unica forma de ver la
        // contraseña generada. QUITAR antes de que esto maneje datos reales
        // -- una contraseña en texto plano en logs es un problema de
        // seguridad real, no solo un detalle de estilo.
        log.warn("[SOLO PRUEBAS] Contrasena temporal para {}: {}", correo, contrasenaTemporal);
        sembrarUsuarioAdmin(empresa, correo, contrasenaTemporal);

        String nombreEmpresa = empresa.getNombreComercial() != null
                ? empresa.getNombreComercial() : empresa.getNombreLegal();
        String url = "https://" + empresa.getDominio();

        enviarCorreoDeBienvenida.ejecutar(new EnviarCorreoDeBienvenida.ComandoBienvenida(
                correo, empresa.getRepresentanteLegal(), nombreEmpresa, url, contrasenaTemporal));

        control.update("""
                update plataforma.tbl_empresas
                set bienvenida_enviada_en = now(), actualizado_en = now()
                where uuid = ?""", empresa.getId());
    }

    private void sembrarUsuarioAdmin(Empresa empresa, String correo, String contrasenaTemporal) {
        JdbcTemplate cliente = jdbcCliente(empresa.getIdentificador().nombreBaseDeDatos());
        String nombre = empresa.getNombreComercial() != null
                ? empresa.getNombreComercial() : empresa.getNombreLegal();

        Long rolId = cliente.queryForObject(
                "select id from seguridad.tbl_roles where nombre = 'ADMIN'", Long.class);

        Long usuarioId = idUsuarioPorCorreo(cliente, correo);
        String hash = cifrador.encode(contrasenaTemporal);
        if (usuarioId == null) {
            cliente.update("""
                    insert into seguridad.tbl_usuarios
                        (correo, hash_contrasena, nombre_completo, es_activo, es_contrasena_temporal)
                    values (?, ?, ?, true, true)""",
                    correo, hash, "Administrador " + nombre);
            usuarioId = cliente.queryForObject(
                    "select id from seguridad.tbl_usuarios where correo = ?", Long.class, correo);
        } else {
            cliente.update("""
                    update seguridad.tbl_usuarios
                    set hash_contrasena = ?, es_contrasena_temporal = true, actualizado_en = now()
                    where id = ?""",
                    hash, usuarioId);
        }

        boolean yaAsignado = existe(cliente, """
                select exists(
                    select 1 from seguridad.tbl_usuarios_roles
                    where usuario_id = ? and rol_id = ?)""", usuarioId, rolId);
        if (!yaAsignado) {
            cliente.update("insert into seguridad.tbl_usuarios_roles (usuario_id, rol_id) values (?, ?)",
                    usuarioId, rolId);
        }
        log.info("Usuario admin sembrado en {}: {}", empresa.getIdentificador().nombreBaseDeDatos(), correo);
    }

    /** id del usuario con ese correo, o null si aun no existe (semilla idempotente). */
    private static Long idUsuarioPorCorreo(JdbcTemplate cliente, String correo) {
        List<Long> ids = cliente.queryForList(
                "select id from seguridad.tbl_usuarios where correo = ?", Long.class, correo);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private static String generarContrasenaTemporal() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALFABETO_CONTRASENA[ALEATORIO.nextInt(ALFABETO_CONTRASENA.length)]);
        }
        return sb.toString();
    }

    private String ultimaMigracionDe(String nombreBd) {
        try {
            String id = jdbcCliente(nombreBd).queryForObject(
                    "select id from public.databasechangelog order by dateexecuted desc, orderexecuted desc limit 1",
                    String.class);
            return id != null ? id : "desconocida";
        } catch (EmptyResultDataAccessException _) {
            return "sin-changelog";
        } catch (RuntimeException e) {
            log.warn("No se pudo leer databasechangelog de {}: {}", nombreBd, e.getMessage());
            return "desconocida";
        }
    }

    private void crearRolSiFalta(String rol, String grupo) {
        // CREATE ROLE / GRANT no admiten parametros para el nombre del rol. rol deriva
        // del slug ya validado y grupo es una constante del codigo; se revalidan aca
        // contra SLUG_SEGURO para dejar la interpolacion demostrablemente segura.
        exigirNombreSeguro(rol);
        exigirNombreSeguro(grupo);
        mantenimiento.execute(
                "do $$ begin "
                        + "if not exists (select from pg_roles where rolname = '" + rol + "') then "
                        + "create role " + rol + " login password '" + rol + "'; "
                        + "grant " + grupo + " to " + rol + "; "
                        + "end if; end $$;");
    }

    private JdbcTemplate jdbcCliente(String nombreBd) {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:postgresql://" + hostCliente + ":" + puertoCliente + "/" + nombreBd,
                ownerUsuario, ownerClave);
        ds.setDriverClassName("org.postgresql.Driver");
        return new JdbcTemplate(ds);
    }

    /**
     * Evalua un `select exists(...)`. Devuelve boolean primitivo para que el llamador
     * no tenga que comprobar null: `select exists` siempre devuelve exactamente una
     * fila con un booleano no nulo.
     */
    private static boolean existe(JdbcTemplate jt, String sqlExists, Object... args) {
        return Boolean.TRUE.equals(jt.queryForObject(sqlExists, Boolean.class, args));
    }

    private static void exigirNombreSeguro(String identificador) {
        if (identificador == null || !identificador.matches(SLUG_SEGURO)) {
            throw new IllegalStateException("Identificador no seguro para DDL: " + identificador);
        }
    }
}
