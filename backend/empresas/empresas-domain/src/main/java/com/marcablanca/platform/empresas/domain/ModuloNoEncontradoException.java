package com.marcablanca.platform.empresas.domain;

public class ModuloNoEncontradoException extends RuntimeException {

    public ModuloNoEncontradoException(String codigo) {
        super("Modulo no encontrado: " + codigo);
    }
}
