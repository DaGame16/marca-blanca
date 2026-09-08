package com.marcablanca.platform.omnicanal.domain;

public class ConversacionNoEncontradaException extends RuntimeException {
    public ConversacionNoEncontradaException(String idContacto) {
        super("No hay conversaciones para el contacto: " + idContacto);
    }
}
