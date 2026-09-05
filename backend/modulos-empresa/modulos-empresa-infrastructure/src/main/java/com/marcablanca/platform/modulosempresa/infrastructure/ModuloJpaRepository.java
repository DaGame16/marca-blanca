package com.marcablanca.platform.modulosempresa.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloJpaRepository extends JpaRepository<ModuloEntity, Long> {
    Optional<ModuloEntity> findByCodigo(String codigo);
}
