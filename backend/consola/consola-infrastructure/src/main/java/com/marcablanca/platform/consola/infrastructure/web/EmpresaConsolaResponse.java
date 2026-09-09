package com.marcablanca.platform.consola.infrastructure.web;

import java.time.Instant;

public record EmpresaConsolaResponse(
        String id,
        String identificador,
        String nombreLegal,
        String dominio,
        String correo,
        String estado,
        String pasoAprovisionamiento,
        String estadoTarea,
        Instant creadaEn) {
}
