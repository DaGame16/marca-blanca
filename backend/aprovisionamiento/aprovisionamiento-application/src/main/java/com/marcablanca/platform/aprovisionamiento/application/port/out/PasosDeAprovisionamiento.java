package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.Empresa;

import java.util.Set;

/**
 * Las operaciones tecnicas del pipeline (Capa 2), cada una idempotente y con
 * credenciales owner. La implementacion vive en infraestructura/pipeline y es
 * la unica pieza que toca DDL. El orden y los reintentos los maneja el servicio.
 */
public interface PasosDeAprovisionamiento {

    /** CREATE DATABASE db_cliente_<slug> TEMPLATE db_plantilla_maestra. */
    void crearBaseDeDatos(Empresa empresa);

    /** Datos propios de la empresa dentro de su base: usuario maestro, marca, config inicial. */
    void aplicarSemilla(Empresa empresa);

    /** Roles de login por-tenant (cli_<slug>_app / _lectura) + secreto en el vault. */
    void crearRolesDeTenant(Empresa empresa);

    /** INSERT en plataforma.tbl_empresa_conexiones (host, puerto, nombre_bd, secreto_ref). */
    void registrarConexion(Empresa empresa);

    /** INSERT en plataforma.tbl_empresa_esquema_version con la ultima migracion aplicada. */
    void registrarVersionDeEsquema(Empresa empresa);

    /** UPSERT en plataforma.tbl_empresa_modulos segun el plan contratado. */
    void poblarModulos(Empresa empresa, Set<String> modulosSolicitados);

    /**
     * Ultimo paso: genera la contrasena temporal, siembra el usuario admin en la
     * base del cliente (es_contrasena_temporal = true) y envia el correo de
     * bienvenida con credenciales y URL. Idempotente via tbl_empresas.bienvenida_enviada_en.
     */
    void enviarBienvenida(Empresa empresa);
}