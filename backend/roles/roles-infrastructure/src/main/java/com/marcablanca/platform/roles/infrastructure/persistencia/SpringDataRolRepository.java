package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataRolRepository extends JpaRepository<RolJpaEntity, Long> {
    Optional<RolJpaEntity> findByUuid(UUID uuid);
    Optional<RolJpaEntity> findByNombre(String nombre);
}
