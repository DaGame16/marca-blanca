package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** tbl_casos_liwa -- un segmento de conversacion con contenido real, listo para analizar. */
public record Caso(Long id, UUID uuid, Long conversacionId, int turnoOrdenInicio, int turnoOrdenFin,
                    boolean esProcesada, boolean esDeAds, OffsetDateTime archivadaEn) {
}
