package com.marcablanca.platform.usuarios.infrastructure.web;

import com.marcablanca.platform.usuarios.domain.Usuario;
import java.util.UUID;

public record UsuarioResponse(UUID uuid, String correo, String nombreCompleto, boolean activo) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getUuid(),
                usuario.getCorreo().valor(),
                usuario.getNombreCompleto(),
                usuario.isActivo()
        );
    }
}