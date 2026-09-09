package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/v1/consola/auth/cambiar-contrasena. El operador sale del token. */
public record CambiarContrasenaOperadorRequest(

        @NotBlank
        String contrasenaActual,

        @NotBlank
        String contrasenaNueva
) {
}
