package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMarcaRepository extends JpaRepository<MarcaDeAprovisionamientoEntity, Long> {
    Optional<MarcaDeAprovisionamientoEntity> findByEmpresaId(Long empresaId);
}
