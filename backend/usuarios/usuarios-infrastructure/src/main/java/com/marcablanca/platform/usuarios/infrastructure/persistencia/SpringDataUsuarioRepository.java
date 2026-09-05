package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByCorreo(String correo);
    Optional<UsuarioJpaEntity> findByUuid(UUID uuid);
}