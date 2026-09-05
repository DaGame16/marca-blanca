package com.marcablanca.platform.modulosempresa.domain;

import java.util.UUID;

/** Propia de modulos-empresa -- no se reutiliza la de empresas.domain, para no acoplar los dos modulos. */
public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(UUID empresaId) {
        super("Empresa no encontrada: " + empresaId);
    }
}
