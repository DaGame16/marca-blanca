package com.marcablanca.platform.roles.domain;

public class RolConUsuariosAsignadosException extends RuntimeException {

    public RolConUsuariosAsignadosException(String nombre) {
        super("El rol '" + nombre + "' tiene usuarios asignados y no se puede eliminar");
    }
}
