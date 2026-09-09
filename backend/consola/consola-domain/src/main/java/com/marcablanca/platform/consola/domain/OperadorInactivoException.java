package com.marcablanca.platform.consola.domain;

/** El operador existe y la contrasena coincide, pero su cuenta esta desactivada. */
public class OperadorInactivoException extends RuntimeException {

    public OperadorInactivoException(String correo) {
        super("El operador " + correo + " esta inactivo.");
    }
}
