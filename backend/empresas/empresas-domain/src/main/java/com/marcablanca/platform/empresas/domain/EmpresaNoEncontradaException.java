package com.marcablanca.platform.empresas.domain;

import java.util.UUID;

public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(UUID empresaId) {
        super("Empresa no encontrada: " + empresaId);
    }
}
