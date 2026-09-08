package com.marcablanca.platform.omnicanal.domain;

public class CasoNoEncontradoException extends RuntimeException {
    public CasoNoEncontradoException(String id) {
        super("Analisis no encontrado: " + id);
    }
}
