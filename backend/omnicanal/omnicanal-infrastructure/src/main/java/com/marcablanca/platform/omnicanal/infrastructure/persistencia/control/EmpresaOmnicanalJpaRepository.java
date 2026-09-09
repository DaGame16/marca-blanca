package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** PENDIENTE -- ver nota en EmpresaOmnicanalEntity. No registrado en el escaneo todavia. */
public interface EmpresaOmnicanalJpaRepository extends JpaRepository<EmpresaOmnicanalEntity, Long> {
    Optional<EmpresaOmnicanalEntity> findByWebhookSecret(String webhookSecret);
}
