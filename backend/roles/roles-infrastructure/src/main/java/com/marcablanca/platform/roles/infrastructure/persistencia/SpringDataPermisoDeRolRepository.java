package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface SpringDataPermisoDeRolRepository extends JpaRepository<PermisoDeRolJpaEntity, Long> {
    List<PermisoDeRolJpaEntity> findByRolId(Long rolId);
    List<PermisoDeRolJpaEntity> findByRolIdIn(Collection<Long> rolIds);
    Optional<PermisoDeRolJpaEntity> findByRolIdAndPermisoId(Long rolId, Long permisoId);
    void deleteByRolIdAndPermisoId(Long rolId, Long permisoId);
}
