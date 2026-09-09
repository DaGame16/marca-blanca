package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/v1/consola/auth/login. Sin identificador de empresa. */
public record LoginOperadorRequest(

        @NotBlank
        @Email
        @Size(max = 254)
        String correo,

        @NotBlank
        String contrasena
) {
}
