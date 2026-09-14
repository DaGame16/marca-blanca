package com.marcablanca.platform.roles.domain;

public class RolDelSistemaException extends RuntimeException {

    public RolDelSistemaException(String nombre) {
        super("El rol '" + nombre + "' es del sistema y no se puede modificar ni eliminar");
    }
}
