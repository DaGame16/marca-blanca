package com.marcablanca.platform.aprovisionamiento.domain;

public class EmpresaNoModificableException extends RuntimeException {
    public EmpresaNoModificableException(EstadoEmpresa estadoActual) {
        super("La empresa ya no esta en borrador (estado: " + estadoActual
                + "); no se puede modificar el registro.");
    }
}
