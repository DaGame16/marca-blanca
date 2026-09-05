package com.marcablanca.platform.modulosempresa.infrastructure.web;

import java.time.Instant;

public record ErrorResponse(int codigo, String mensaje, Instant marcaDeTiempo, String ruta) {
}
