package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.domain.Rol;

final class RolMapper {

    private RolMapper() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    static Rol aDominio(RolJpaEntity entidad) {
        return new Rol(entidad.getId(), entidad.getUuid(), entidad.getNombre(),
                entidad.getDescripcion(), entidad.isEsDelSistema());
    }

    static RolJpaEntity aEntidad(Rol dominio) {
        return new RolJpaEntity(dominio.getId(), dominio.getUuid(), dominio.getNombre(),
                dominio.getDescripcion(), dominio.isEsDelSistema());
    }
}
