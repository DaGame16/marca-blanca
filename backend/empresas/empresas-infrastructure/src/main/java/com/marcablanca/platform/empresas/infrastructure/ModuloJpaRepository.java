package com.marcablanca.platform.empresas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ModuloJpaRepository extends JpaRepository<ModuloEntity, Long> {
    Optional<ModuloEntity> findByCodigo(String codigo);
}
