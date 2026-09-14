package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataUsuarioRolRepository extends JpaRepository<UsuarioRolJpaEntity, Long> {
    List<UsuarioRolJpaEntity> findByUsuarioId(Long usuarioId);
    Optional<UsuarioRolJpaEntity> findByUsuarioIdAndRolId(Long usuarioId, Long rolId);
    long countByRolId(Long rolId);
    void deleteByUsuarioIdAndRolId(Long usuarioId, Long rolId);
}
