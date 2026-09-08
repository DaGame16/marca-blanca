package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Turno ya persistido -- tbl_turnos_conversacion_liwa. */
public record Turno(Long id, UUID uuid, Long conversacionId, int orden, AutorTurno autor,
                     String nombreAutor, String mensaje, OffsetDateTime ocurridoEn) {
}
