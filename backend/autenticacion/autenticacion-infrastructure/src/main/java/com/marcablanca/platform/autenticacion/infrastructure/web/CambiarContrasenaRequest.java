package com.marcablanca.platform.autenticacion.infrastructure.web;

public record CambiarContrasenaRequest(String contrasenaActual, String contrasenaNueva) {
}
