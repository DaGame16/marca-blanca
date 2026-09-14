package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPermisoRepository extends JpaRepository<PermisoJpaEntity, Long> {
    Optional<PermisoJpaEntity> findByUuid(UUID uuid);
}
