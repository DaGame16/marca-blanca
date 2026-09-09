package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de PUT /api/v1/consola/empresas/{id}/datos. */
public record ActualizarDatosRequest(

        @NotBlank @Size(max = 200) String nombreLegal,
        @NotBlank @Size(max = 200) String representanteLegal,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(max = 40) String telefono,
        @NotBlank @Size(max = 255) String sitioWeb
) {
}
