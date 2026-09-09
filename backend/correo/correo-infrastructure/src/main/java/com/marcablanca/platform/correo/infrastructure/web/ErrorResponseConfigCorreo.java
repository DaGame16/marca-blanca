package com.marcablanca.platform.correo.infrastructure.web;

import java.time.Instant;

public record ErrorResponseConfigCorreo(int codigo, String mensaje, Instant marcaDeTiempo, String ruta) {
}
