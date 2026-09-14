package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.domain.Rol;

import java.util.UUID;

public record RolResponse(UUID uuid, String nombre, String descripcion, boolean esDelSistema) {
    public static RolResponse desde(Rol rol) {
        return new RolResponse(rol.getUuid(), rol.getNombre(), rol.getDescripcion(), rol.isEsDelSistema());
    }
}
