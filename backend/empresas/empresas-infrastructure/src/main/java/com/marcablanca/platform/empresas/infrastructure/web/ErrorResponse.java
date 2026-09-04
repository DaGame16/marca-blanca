package com.marcablanca.platform.empresas.infrastructure.web;

import java.time.Instant;

public record ErrorResponse(int codigo, String mensaje, Instant marcaDeTiempo, String ruta) {
}
