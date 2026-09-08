package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/v1/registro/empresas (formulario publico del paso 1). */
public record RegistrarEmpresaRequest(

        @NotBlank
        @Size(max = 200)
        String nombreEmpresa,

        @NotBlank
        @Size(max = 200)
        String representanteLegal,

        @NotBlank
        @Email
        @Size(max = 254)
        String correo,

        @NotBlank
        @Size(max = 40)
        String telefono,

        @NotBlank
        @Size(max = 255)
        String sitioWeb
) {
}
