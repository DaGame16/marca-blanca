package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EstadoEmpresa;
import com.marcablanca.platform.aprovisionamiento.domain.HashContrasenaMaestra;
import com.marcablanca.platform.aprovisionamiento.domain.Identificador;

final class EmpresaMapper {

    private EmpresaMapper() {
    }

    static Empresa aDominio(EmpresaDeAprovisionamientoEntity e) {
        return new Empresa(
                e.getUuid(),
                new Identificador(e.getIdentificador()),
                e.getNombreLegal(),
                e.getNombreComercial(),
                e.getDominio(),
                e.getHashContrasenaMaestra() == null ? null : new HashContrasenaMaestra(e.getHashContrasenaMaestra()),
                EstadoEmpresa.valueOf(e.getEstado().toUpperCase())
        );
    }

    /** Vuelca el estado del agregado sobre la entidad (misma entidad si ya existia -> UPDATE). */
    static void aplicar(Empresa d, EmpresaDeAprovisionamientoEntity e) {
        e.setUuid(d.getId());
        e.setIdentificador(d.getIdentificador().valor());
        e.setNombreLegal(d.getNombreLegal());
        e.setNombreComercial(d.getNombreComercial());
        e.setDominio(d.getDominio());
        e.setHashContrasenaMaestra(
                d.getHashContrasenaMaestra() == null ? null : d.getHashContrasenaMaestra().valor());
        e.setEstado(d.getEstado().name().toLowerCase());
    }
}