package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo de POST/PUT /api/v1/consola/modulos. En PUT el {@code codigo} se ignora
 * (identifica el modulo en tbl_empresa_modulos y no se cambia).
 */
public record ModuloRequest(

        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 255) String descripcion,
        @NotNull @PositiveOrZero BigDecimal precio,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String moneda
) {
}
