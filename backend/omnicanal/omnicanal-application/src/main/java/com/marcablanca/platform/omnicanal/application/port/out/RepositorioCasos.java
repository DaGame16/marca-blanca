package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.Caso;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioCasos {

    Caso crear(Long conversacionId, int turnoOrdenInicio, int turnoOrdenFin, boolean esDeAds);

    Optional<Caso> buscarPorId(Long id);

    void marcarProcesada(Long id, boolean procesada);

    List<Caso> listarPendientes(OffsetDateTime desde, OffsetDateTime hasta);

    List<Caso> listarSinReanalizar(OffsetDateTime desde, OffsetDateTime hasta);

    /** Casos de un contacto (via su conversacion), del mas reciente al mas viejo. */
    List<Caso> listarPorConversacion(Long conversacionId);

    void marcarEsDeAds(Long casoId, boolean esDeAds);

    /** Volumen de casos por periodo. `agrupacion`: "dia" | "semana" | "mes". */
    List<ConteoPorPeriodo> serieTemporal(String agrupacion, OffsetDateTime desde, OffsetDateTime hasta);

    record ConteoPorPeriodo(String periodo, long total) {
    }

    long contarPendientes(OffsetDateTime desde, OffsetDateTime hasta);

    long contarSinReanalizar(OffsetDateTime desde, OffsetDateTime hasta);

    long contarTotal(OffsetDateTime desde, OffsetDateTime hasta);

    long contarConAnalisisNuevo(OffsetDateTime desde, OffsetDateTime hasta);
}
