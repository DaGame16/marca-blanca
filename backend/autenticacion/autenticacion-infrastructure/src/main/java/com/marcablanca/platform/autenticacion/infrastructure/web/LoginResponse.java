package com.marcablanca.platform.autenticacion.infrastructure.web;

import java.util.UUID;

public record LoginResponse(UUID usuarioId, String token) {
}
