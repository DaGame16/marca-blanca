package com.marcablanca.platform.aprovisionamiento.domain;

/**
 * La empresa esta en un estado en el que la consola no puede editar sus datos,
 * marca o modulos (BORRADOR es del wizard; INACTIVA no se toca).
 */
public class EmpresaNoEditableException extends RuntimeException {
    public EmpresaNoEditableException(EstadoEmpresa estadoActual) {
        super("La empresa no se puede editar. Estado actual: " + estadoActual);
    }
}
