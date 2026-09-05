package com.marcablanca.platform.usuarios.domain;

import java.util.UUID;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(UUID uuid) {
        super("No existe un usuario con uuid: " + uuid);
    }
}