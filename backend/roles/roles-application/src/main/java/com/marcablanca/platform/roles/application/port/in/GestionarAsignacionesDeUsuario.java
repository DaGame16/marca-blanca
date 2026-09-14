package com.marcablanca.platform.roles.application.port.in;

import com.marcablanca.platform.roles.domain.Rol;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GestionarAsignacionesDeUsuario {

    void asignarRol(UUID usuarioUuid, UUID rolUuid);

    void quitarRol(UUID usuarioUuid, UUID rolUuid);

    List<Rol> listarRolesDeUsuario(UUID usuarioUuid);

    void concederPermiso(UUID usuarioUuid, UUID permisoUuid);

    void revocarPermiso(UUID usuarioUuid, UUID permisoUuid);

    void quitarAjustePermiso(UUID usuarioUuid, UUID permisoUuid);

    /** Clave = nombre del permiso, valor = es_concedido. */
    Map<String, Boolean> listarAjustesDeUsuario(UUID usuarioUuid);
}
