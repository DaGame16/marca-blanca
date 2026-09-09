package com.marcablanca.platform.consola.domain;

/** Correo inexistente o contrasena que no coincide al autenticar un operador. */
public class CredencialesDeOperadorInvalidasException extends RuntimeException {

    public CredencialesDeOperadorInvalidasException() {
        super("Correo o contrasena de operador incorrectos.");
    }
}
