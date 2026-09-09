package com.marcablanca.platform.aprovisionamiento.domain;

/** Solo una empresa SUSPENDIDA se puede reactivar. */
public class EmpresaNoReactivableException extends RuntimeException {
    public EmpresaNoReactivableException(EstadoEmpresa estadoActual) {
        super("La empresa no se puede reactivar. Estado actual: " + estadoActual);
    }
}
