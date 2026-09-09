package com.marcablanca.platform.consola.infrastructure.seguridad;

import java.util.UUID;

/** Datos que el filtro extrae de un token de operador valido. */
public record OperadorAutenticado(UUID operadorId, String rol, boolean debeCambiarContrasena) {
}
