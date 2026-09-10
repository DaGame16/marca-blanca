package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRefDeOmnicanalJpaRepository extends JpaRepository<EmpresaRefDeOmnicanal, Long> {
    Optional<EmpresaRefDeOmnicanal> findByIdentificador(String identificador);
}
