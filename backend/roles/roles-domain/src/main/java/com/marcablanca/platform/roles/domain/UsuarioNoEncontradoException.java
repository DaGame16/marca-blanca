package com.marcablanca.platform.roles.domain;

import java.util.UUID;

/**
 * Propia de este contexto (roles no conoce el tipo Usuario de usuarios-domain,
 * solo su uuid) -- analoga a la UsuarioNoEncontradoException de usuarios-domain,
 * pero deliberadamente separada; cada bounded context declara sus propias
 * excepciones de dominio.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(UUID uuid) {
        super("No existe un usuario con uuid " + uuid);
    }
}
