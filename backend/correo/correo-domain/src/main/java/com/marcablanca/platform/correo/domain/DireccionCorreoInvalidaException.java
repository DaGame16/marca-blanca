package com.marcablanca.platform.correo.domain;

public class DireccionCorreoInvalidaException extends RuntimeException {
    public DireccionCorreoInvalidaException(String valor) {
        super("Direccion de correo invalida: " + valor);
    }
}
