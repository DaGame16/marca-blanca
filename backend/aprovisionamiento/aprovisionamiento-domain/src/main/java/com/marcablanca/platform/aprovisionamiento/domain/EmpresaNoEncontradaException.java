package com.marcablanca.platform.aprovisionamiento.domain;

import java.util.UUID;

public class EmpresaNoEncontradaException extends RuntimeException {
    public EmpresaNoEncontradaException(UUID empresaId) {
        super("No existe la empresa " + empresaId + ".");
    }
}
