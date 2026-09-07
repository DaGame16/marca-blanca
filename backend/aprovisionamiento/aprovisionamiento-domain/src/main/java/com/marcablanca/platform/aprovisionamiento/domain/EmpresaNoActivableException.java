package com.marcablanca.platform.aprovisionamiento.domain;

public class EmpresaNoActivableException extends RuntimeException {
    public EmpresaNoActivableException(EstadoEmpresa estadoActual) {
        super("La empresa no se puede activar. Estado actual: " + estadoActual);
    }
}