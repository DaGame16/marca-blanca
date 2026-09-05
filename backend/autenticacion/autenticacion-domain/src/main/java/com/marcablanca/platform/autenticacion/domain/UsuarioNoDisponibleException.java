package com.marcablanca.platform.autenticacion.domain;

/** Propia de autenticacion -- ya no se usa la de usuarios.domain (ver AdaptadorVerificadorDeUsuarios). */
public class UsuarioNoDisponibleException extends RuntimeException {
    public UsuarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
