package com.marcablanca.platform.roles.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUsuarioRefRepository extends JpaRepository<UsuarioRefDeRoles, Long> {
    Optional<UsuarioRefDeRoles> findByUuid(UUID uuid);
}
