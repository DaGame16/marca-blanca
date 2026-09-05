package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataUsuarioPerfilRepository extends JpaRepository<UsuarioPerfilJpaEntity, Long> {
    Optional<UsuarioPerfilJpaEntity> findByUsuarioId(Long usuarioId);
}