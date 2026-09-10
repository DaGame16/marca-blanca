package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarReportesOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class ConsultarReportesOmnicanalService implements ConsultarReportesOmnicanal {

    private static final String MOTIVO_SOPORTE = "soporte";

    private final RepositorioAnalisis repositorioAnalisis;
    private final RepositorioCasos repositorioCasos;

    public ConsultarReportesOmnicanalService(RepositorioAnalisis repositorioAnalisis, RepositorioCasos repositorioCasos) {
        this.repositorioAnalisis = repositorioAnalisis;
        this.repositorioCasos = repositorioCasos;
    }

    @Override
    public EstadisticasOmnicanal estadisticas(String agrupacion, OffsetDateTime desde, OffsetDateTime hasta) {
        String agr = agrupacion == null ? "dia" : agrupacion;
        long total = repositorioCasos.contarTotal(desde, hasta);
        List<PuntoSerie> serie = repositorioCasos.serieTemporal(agr, desde, hasta).stream()
                .map(p -> new PuntoSerie(p.periodo(), p.total()))
                .toList();
        return new EstadisticasOmnicanal(desde, hasta, agr, total, serie);
    }

    @Override
    public DistribucionSentimientoOmnicanal distribucionSentimientoOmnicanal(OffsetDateTime desde, OffsetDateTime hasta) {
        Map<String, Long> inicial = repositorioAnalisis.distribucionSentimientoInicial(desde, hasta);
        Map<String, Long> fin = repositorioAnalisis.distribucionSentimientoFinal(desde, hasta);
        return new DistribucionSentimientoOmnicanal(inicial, fin);
    }

    @Override
    public ResumenSoporteOmnicanal resumenSoporteOmnicanal(OffsetDateTime desde, OffsetDateTime hasta) {
        long total = repositorioAnalisis.contarPorMotivo(MOTIVO_SOPORTE, desde, hasta);
        long resueltos = repositorioAnalisis.contarPorMotivoYResultado(MOTIVO_SOPORTE, "resuelto", desde, hasta);
        long escalados = repositorioAnalisis.contarPorMotivoYResultado(MOTIVO_SOPORTE, "escalado", desde, hasta);
        return new ResumenSoporteOmnicanal(total, resueltos, escalados, total - resueltos - escalados);
    }

    @Override
    public ResumenVentasOmnicanal resumenVentasOmnicanal(OffsetDateTime desde, OffsetDateTime hasta) {
        long totalOportunidades = repositorioAnalisis.contarOportunidadVenta(false, desde, hasta);
        long confirmadas = repositorioAnalisis.contarOportunidadVenta(true, desde, hasta);
        double tasa = totalOportunidades > 0 ? Math.round((confirmadas * 1000.0 / totalOportunidades)) / 10.0 : 0;
        return new ResumenVentasOmnicanal(totalOportunidades, confirmadas, tasa);
    }

    @Override
    public ResumenAdsOmnicanal resumenAdsOmnicanal(OffsetDateTime desde, OffsetDateTime hasta) {
        long total = repositorioAnalisis.contarPorAds("total", desde, hasta);
        Map<String, Long> porMotivo = repositorioAnalisis.agruparPorMotivoConAds(desde, hasta);
        Map<String, Long> porResultado = repositorioAnalisis.agruparPorResultadoConAds(desde, hasta);
        return new ResumenAdsOmnicanal(total, porMotivo, porResultado);
    }
}
