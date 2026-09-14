package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.domain.Permiso;

import java.util.UUID;

public record PermisoResponse(UUID uuid, String nombre, String descripcion) {
    public static PermisoResponse desde(Permiso permiso) {
        return new PermisoResponse(permiso.getUuid(), permiso.getNombre(), permiso.getDescripcion());
    }
}
