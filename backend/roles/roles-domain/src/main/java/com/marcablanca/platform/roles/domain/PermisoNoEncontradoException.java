package com.marcablanca.platform.roles.domain;

import java.util.UUID;

public class PermisoNoEncontradoException extends RuntimeException {

    public PermisoNoEncontradoException(UUID uuid) {
        super("No existe un permiso con uuid " + uuid);
    }
}
