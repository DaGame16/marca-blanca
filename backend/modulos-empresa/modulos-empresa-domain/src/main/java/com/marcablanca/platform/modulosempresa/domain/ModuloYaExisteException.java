package com.marcablanca.platform.modulosempresa.domain;

/** Ya hay un modulo con ese codigo en el catalogo. */
public class ModuloYaExisteException extends RuntimeException {
    public ModuloYaExisteException(String codigo) {
        super("Ya existe un modulo con el codigo '" + codigo + "'.");
    }
}
