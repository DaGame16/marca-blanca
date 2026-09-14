package com.marcablanca.platform.roles.domain;

import java.util.UUID;

public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(UUID uuid) {
        super("No existe un rol con uuid " + uuid);
    }
}
