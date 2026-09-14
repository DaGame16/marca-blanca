package com.marcablanca.platform.roles.domain;

public class RolYaExisteException extends RuntimeException {

    public RolYaExisteException(String nombre) {
        super("Ya existe un rol con nombre '" + nombre + "'");
    }
}
