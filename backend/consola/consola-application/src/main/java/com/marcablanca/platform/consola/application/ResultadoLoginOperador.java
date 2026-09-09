package com.marcablanca.platform.consola.application;

import java.util.UUID;

/**
 * Salida de un login de operador exitoso. {@code debeCambiarContrasena} en true
 * significa que el token solo sirve para {@code /api/v1/consola/auth/**} hasta que
 * el operador cambie la contrasena temporal.
 */
public record ResultadoLoginOperador(
        UUID operadorId,
        String correo,
        String rol,
        String token,
        boolean debeCambiarContrasena) {
}
