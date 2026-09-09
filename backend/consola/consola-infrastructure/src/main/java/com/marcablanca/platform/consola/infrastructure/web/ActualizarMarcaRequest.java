package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/v1/consola/empresas/{id}/marca. Colores y logo opcionales
 * (null / vacio = sin definir); el formato del color lo valida el dominio.
 */
public record ActualizarMarcaRequest(

        @Size(max = 20) String colorPrimario,
        @Size(max = 20) String colorSecundario,
        @Size(max = 500) String urlLogo,
        @Min(1) @Max(3) int tipoLogin,
        @Min(1) @Max(3) int tipoPantallaPrincipal
) {
}
