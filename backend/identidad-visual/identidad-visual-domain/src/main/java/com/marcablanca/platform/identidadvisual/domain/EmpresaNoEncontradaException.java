package com.marcablanca.platform.identidadvisual.domain;

public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(String identificadorEmpresa) {
        super("Empresa no encontrada: " + identificadorEmpresa);
    }
}
