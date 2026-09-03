package com.marcablanca.platform.autenticacion.application;

import java.util.UUID;

public record ResultadoAutenticacion(UUID usuarioId, String token) {
}
