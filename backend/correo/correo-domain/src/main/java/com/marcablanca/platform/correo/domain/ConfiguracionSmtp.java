package com.marcablanca.platform.correo.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * tbl_config_correo -- config SMTP de la plataforma. Como maximo una fila
 * con esActiva=true. La clave real NUNCA viaja por este record -- solo se
 * expone si esta o no configurada, para que el admin sepa si falta ponerla.
 */
public record ConfiguracionSmtp(
        Long id, UUID uuid,
        String remitenteNombre, String remitenteCorreo, String responderA,
        String host, int puerto, String usuario, String secretoRef,
        String seguridad, boolean esActiva, boolean claveConfigurada,
        OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
}
