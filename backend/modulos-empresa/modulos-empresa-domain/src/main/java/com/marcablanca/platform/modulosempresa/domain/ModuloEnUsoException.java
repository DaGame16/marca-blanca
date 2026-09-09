package com.marcablanca.platform.modulosempresa.domain;

/** No se puede borrar un modulo que alguna empresa tiene asignado. */
public class ModuloEnUsoException extends RuntimeException {
    public ModuloEnUsoException(String codigo) {
        super("El modulo '" + codigo + "' esta asignado a una o mas empresas; no se puede eliminar.");
    }
}
