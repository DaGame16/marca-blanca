package com.marcablanca.platform.roles.application.port.in;

import com.marcablanca.platform.roles.domain.Permiso;
import com.marcablanca.platform.roles.domain.Rol;

import java.util.List;
import java.util.UUID;

public interface GestionarRoles {

    Rol crear(String nombre, String descripcion);

    Rol actualizar(UUID uuid, String nombre, String descripcion);

    void eliminar(UUID uuid);

    Rol consultarPorUuid(UUID uuid);

    List<Rol> listar();

    void asignarPermiso(UUID rolUuid, UUID permisoUuid);

    void quitarPermiso(UUID rolUuid, UUID permisoUuid);

    List<Permiso> listarPermisosDeRol(UUID rolUuid);
}
