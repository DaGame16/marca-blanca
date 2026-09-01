package com.miempresa.miproyecto.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email(message = "Email inválido") String email,
        @NotBlank String password
) {
}
