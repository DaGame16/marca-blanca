package com.marcablanca.platform.usuarios.application;

import java.util.UUID;

public record ResultadoAutenticacion(UUID usuarioId, String token) {
}
