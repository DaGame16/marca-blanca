package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.domain.Caso;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
class RepositorioCasosJpa implements RepositorioCasos {

    private final CasoJpaRepository casos;

    RepositorioCasosJpa(CasoJpaRepository casos) {
        this.casos = casos;
    }

    @Override
    public Caso crear(Long conversacionId, int turnoOrdenInicio, int turnoOrdenFin, boolean esDeAds) {
        return mapear(casos.save(new CasoEntity(conversacionId, turnoOrdenInicio, turnoOrdenFin, esDeAds)));
    }

    @Override
    public Optional<Caso> buscarPorId(Long id) {
        return casos.findById(id).map(this::mapear);
    }

    @Override
    public void marcarProcesada(Long id, boolean procesada) {
        casos.findById(id).ifPresent(e -> {
            e.marcarProcesada(procesada);
            casos.save(e);
        });
    }

    @Override
    public List<Caso> listarPendientes(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.listarPendientes(desde, hasta).stream().map(this::mapear).toList();
    }

    @Override
    public List<Caso> listarSinReanalizar(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.listarSinReanalizar(desde, hasta).stream().map(this::mapear).toList();
    }

    @Override
    public long contarPendientes(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.contarPendientes(desde, hasta);
    }

    @Override
    public long contarSinReanalizar(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.contarSinReanalizar(desde, hasta);
    }

    @Override
    public long contarTotal(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.contarTotal(desde, hasta);
    }

    @Override
    public long contarConAnalisisNuevo(OffsetDateTime desde, OffsetDateTime hasta) {
        return casos.contarConAnalisisNuevo(desde, hasta);
    }

    private Caso mapear(CasoEntity e) {
        return new Caso(e.getId(), e.getUuid(), e.getConversacionId(), e.getTurnoOrdenInicio(), e.getTurnoOrdenFin(),
                e.isEsProcesada(), e.isEsDeAds(), e.getArchivadaEn());
    }
}
