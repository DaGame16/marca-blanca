package com.marcablanca.platform.consola.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST/PUT /api/v1/consola/config-correo. {@code usuario} y
 * {@code secretoRef} son opcionales (no todo SMTP exige autenticacion).
 */
public record ConfigCorreoRequest(

        @Size(max = 150) String remitenteNombre,
        @NotBlank @Email @Size(max = 254) String remitenteCorreo,
        @Email @Size(max = 254) String responderA,
        @NotBlank @Size(max = 255) String host,
        @Min(1) @Max(65535) int puerto,
        @Size(max = 255) String usuario,
        @Size(max = 400) String secretoRef,
        @NotBlank @Pattern(regexp = "ninguna|starttls|ssl") String seguridad
) {
}
