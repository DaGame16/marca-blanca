package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.domain.EstadoTarea;
import com.marcablanca.platform.aprovisionamiento.domain.PasoDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.TareaDeAprovisionamiento;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

final class TareaDeAprovisionamientoMapper {

    private TareaDeAprovisionamientoMapper() {
    }

    static TareaDeAprovisionamiento aDominio(TareaDeAprovisionamientoEntity e, UUID empresaUuid) {
        return TareaDeAprovisionamiento.reconstituir(new TareaDeAprovisionamiento.Instantanea(
                e.getUuid(),
                empresaUuid,
                e.getNombreBd(),
                PasoDeAprovisionamiento.valueOf(e.getPasoActual().toUpperCase()),
                EstadoTarea.valueOf(e.getEstado().toUpperCase()),
                e.getIntentos(),
                e.getMaxIntentos(),
                e.getUltimoError(),
                e.getDisponibleEn() == null ? null : e.getDisponibleEn().toInstant()
        ));
    }

    static void aplicar(TareaDeAprovisionamiento d, Long empresaIdInterno, TareaDeAprovisionamientoEntity e) {
        e.setUuid(d.getId());
        e.setEmpresaId(empresaIdInterno);
        e.setNombreBd(d.getNombreBd());
        e.setPasoActual(d.getPaso().name().toLowerCase());
        e.setEstado(d.getEstado().name().toLowerCase());
        e.setIntentos(d.getIntentos());
        e.setMaxIntentos(d.getMaxIntentos());
        e.setUltimoError(d.getUltimoError());
        e.setDisponibleEn(d.getDisponibleEn() == null
                ? null
                : OffsetDateTime.ofInstant(d.getDisponibleEn(), ZoneOffset.UTC));
        if (d.getEstado() == EstadoTarea.COMPLETADO) {
            e.setCompletadoEn(OffsetDateTime.now(ZoneOffset.UTC));
        }
    }
}