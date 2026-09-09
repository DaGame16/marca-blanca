package com.marcablanca.platform.consola.application;

import java.time.Instant;

/** Fila del listado de empresas en la consola de operacion. */
public record EmpresaParaConsola(
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
