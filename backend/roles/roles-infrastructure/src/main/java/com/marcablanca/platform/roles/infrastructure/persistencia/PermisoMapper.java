package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.domain.Permiso;

final class PermisoMapper {

    private PermisoMapper() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    static Permiso aDominio(PermisoJpaEntity entidad) {
        return new Permiso(entidad.getId(), entidad.getUuid(), entidad.getNombre(), entidad.getDescripcion());
    }
}
