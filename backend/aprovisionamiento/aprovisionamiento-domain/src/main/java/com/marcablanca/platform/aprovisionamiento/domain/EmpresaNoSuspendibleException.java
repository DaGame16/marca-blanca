package com.marcablanca.platform.aprovisionamiento.domain;

/** Solo una empresa ACTIVA se puede suspender. */
public class EmpresaNoSuspendibleException extends RuntimeException {
    public EmpresaNoSuspendibleException(EstadoEmpresa estadoActual) {
        super("La empresa no se puede suspender. Estado actual: " + estadoActual);
    }
}
