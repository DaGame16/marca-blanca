package com.marcablanca.platform.modulosempresa.domain;

public class ModuloNoEncontradoException extends RuntimeException {

    public ModuloNoEncontradoException(String codigo) {
        super("Modulo no encontrado: " + codigo);
    }
}
