package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionOmnicanalJpaRepository extends JpaRepository<ConfiguracionOmnicanalEntity, Long> {

    Optional<ConfiguracionOmnicanalEntity> findFirstByOrderByIdAsc();
}
