package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataTareaDeAprovisionamientoRepository
        extends JpaRepository<TareaDeAprovisionamientoEntity, Long> {

    Optional<TareaDeAprovisionamientoEntity> findByUuid(UUID uuid);

    Optional<TareaDeAprovisionamientoEntity> findByEmpresaId(Long empresaId);
}
