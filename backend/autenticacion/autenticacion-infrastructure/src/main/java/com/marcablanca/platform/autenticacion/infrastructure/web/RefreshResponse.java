package com.marcablanca.platform.autenticacion.infrastructure.web;

import java.util.UUID;

public record RefreshResponse(UUID usuarioId, String token, String refreshToken) {
}
