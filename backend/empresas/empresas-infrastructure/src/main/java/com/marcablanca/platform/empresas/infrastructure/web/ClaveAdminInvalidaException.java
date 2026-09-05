package com.marcablanca.platform.empresas.infrastructure.web;

/**
 * No es una regla de dominio -- es un detalle de infraestructura (falta o
 * no coincide el header X-Admin-Key), por eso vive en "web", no en "domain".
 */
public class ClaveAdminInvalidaException extends RuntimeException {

    public ClaveAdminInvalidaException() {
        super("Clave de administrador invalida o ausente.");
    }
}
