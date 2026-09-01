package com.marcablanca.platform.usuarios.domain;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("El correo o la contrasena son incorrectos.");
    }
}
