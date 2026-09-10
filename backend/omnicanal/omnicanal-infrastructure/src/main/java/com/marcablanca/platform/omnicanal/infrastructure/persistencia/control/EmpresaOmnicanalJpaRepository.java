package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaOmnicanalJpaRepository extends JpaRepository<EmpresaOmnicanalEntity, Long> {
    Optional<EmpresaOmnicanalEntity> findByWebhookSecret(String webhookSecret);
}
