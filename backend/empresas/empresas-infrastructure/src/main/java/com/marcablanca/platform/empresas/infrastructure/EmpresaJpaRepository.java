package com.marcablanca.platform.empresas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    Optional<EmpresaEntity> findByIdentificadorAndEstado(String identificador, String estado);
}
