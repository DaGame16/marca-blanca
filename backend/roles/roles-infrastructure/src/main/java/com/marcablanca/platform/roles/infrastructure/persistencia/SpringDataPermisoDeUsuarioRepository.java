package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataPermisoDeUsuarioRepository extends JpaRepository<PermisoDeUsuarioJpaEntity, Long> {
    List<PermisoDeUsuarioJpaEntity> findByUsuarioId(Long usuarioId);
    Optional<PermisoDeUsuarioJpaEntity> findByUsuarioIdAndPermisoId(Long usuarioId, Long permisoId);
    void deleteByUsuarioIdAndPermisoId(Long usuarioId, Long permisoId);
}
