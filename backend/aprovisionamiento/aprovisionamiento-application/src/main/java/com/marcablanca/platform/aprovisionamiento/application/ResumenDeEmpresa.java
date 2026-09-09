package com.marcablanca.platform.aprovisionamiento.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Vista de lectura de una empresa para paneles de administracion. No es el
 * agregado: trae solo lo que se muestra en una tabla, incluido el estado de la
 * tarea de aprovisionamiento si existe (null si nunca se disparo el pipeline).
 */
public record ResumenDeEmpresa(
        UUID id,
        String identificador,
        String nombreLegal,
        String dominio,
        String correo,
        String estado,
        String pasoAprovisionamiento,
        String estadoTarea,
        Instant creadaEn) {
}
