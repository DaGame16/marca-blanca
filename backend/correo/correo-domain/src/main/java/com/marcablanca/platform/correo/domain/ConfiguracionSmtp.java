package com.marcablanca.platform.correo.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** tbl_config_correo -- config SMTP de la plataforma. Como maximo una fila con esActiva=true. */
public record ConfiguracionSmtp(
        Long id, UUID uuid,
        String remitenteNombre, String remitenteCorreo, String responderA,
        String host, int puerto, String usuario, String secretoRef,
        String seguridad, boolean esActiva,
        OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
}
