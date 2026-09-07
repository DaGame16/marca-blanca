package com.marcablanca.platform.aprovisionamiento.domain;

public class EmpresaYaExisteException extends RuntimeException {
    public EmpresaYaExisteException(String detalle) {
        super("Ya existe una empresa con " + detalle + ".");
    }
}