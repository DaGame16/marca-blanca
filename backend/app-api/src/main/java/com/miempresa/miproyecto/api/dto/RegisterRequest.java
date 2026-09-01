package com.miempresa.miproyecto.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") String fullName,
        @NotBlank @Email(message = "Email inválido") String email,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password
) {
}
