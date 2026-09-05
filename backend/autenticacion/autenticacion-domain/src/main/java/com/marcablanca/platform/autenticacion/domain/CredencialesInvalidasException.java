package com.marcablanca.platform.autenticacion.domain;

/** Propia de autenticacion -- ya no se usa la de usuarios.domain (ver AdaptadorVerificadorDeUsuarios). */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("El correo o la contrasena son incorrectos.");
    }
}
