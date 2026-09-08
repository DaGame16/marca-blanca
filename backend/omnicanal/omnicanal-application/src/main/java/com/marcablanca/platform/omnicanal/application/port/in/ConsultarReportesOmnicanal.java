package com.marcablanca.platform.omnicanal.application.port.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface ConsultarReportesOmnicanal {

    record EstadisticasOmnicanal(OffsetDateTime desde, OffsetDateTime hasta, String agrupacion,
                                  long totalCasos, List<PuntoSerie> serieTemporal) {
    }

    record PuntoSerie(String periodo, long total) {
    }

    EstadisticasOmnicanal estadisticas(String agrupacion, OffsetDateTime desde, OffsetDateTime hasta);

    record DistribucionSentimientoOmnicanal(Map<String, Long> sentimientoInicial, Map<String, Long> sentimientoFinal) {
    }

    DistribucionSentimientoOmnicanal distribucionSentimientoOmnicanal(OffsetDateTime desde, OffsetDateTime hasta);

    record ResumenSoporteOmnicanal(long totalSoporte, long resueltos, long escalados, long noResueltos) {
    }

    ResumenSoporteOmnicanal resumenSoporteOmnicanal(OffsetDateTime desde, OffsetDateTime hasta);

    record ResumenVentasOmnicanal(long totalOportunidades, long confirmadasEnTexto, double tasaConversionTexto) {
    }

    ResumenVentasOmnicanal resumenVentasOmnicanal(OffsetDateTime desde, OffsetDateTime hasta);

    record ResumenAdsOmnicanal(long totalCasosDeAds, Map<String, Long> porMotivo, Map<String, Long> porResultado) {
    }

    ResumenAdsOmnicanal resumenAdsOmnicanal(OffsetDateTime desde, OffsetDateTime hasta);
}
