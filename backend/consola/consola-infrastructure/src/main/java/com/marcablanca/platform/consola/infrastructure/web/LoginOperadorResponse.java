package com.marcablanca.platform.consola.infrastructure.web;

import java.util.UUID;

public record LoginOperadorResponse(
        UUID operadorId,
        String correo,
        String rol,
        String token,
        boolean debeCambiarContrasena) {
}
