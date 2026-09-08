package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

/**
 * Cuerpo de PUT /api/v1/registro/empresas/{empresaId}/personalizacion.
 * Reemplazo total: el wizard manda todo lo acumulado. tipoLogin / tipoPantallaPrincipal
 * caen en 1 si no vienen.
 */
public record PersonalizacionRequest(
        String colorPrimario,
        String colorSecundario,
        String urlLogo,
        Integer tipoLogin,
        Integer tipoPantallaPrincipal
) {
}
