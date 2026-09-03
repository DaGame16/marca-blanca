package com.marcablanca.platform.autenticacion.infrastructure.web;

public record LoginRequest(String correo, String contrasena, String identificadorEmpresa) {
}
