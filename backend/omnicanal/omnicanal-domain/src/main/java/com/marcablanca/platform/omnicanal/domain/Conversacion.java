package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** tbl_conversaciones_liwa -- una fila por contacto. */
public record Conversacion(Long id, UUID uuid, String idContacto, String nombreContacto,
                            String historialChatCompleto, String datosCrudosJson, boolean esDeAds,
                            OffsetDateTime creadoEn, OffsetDateTime archivadaEn) {
}
