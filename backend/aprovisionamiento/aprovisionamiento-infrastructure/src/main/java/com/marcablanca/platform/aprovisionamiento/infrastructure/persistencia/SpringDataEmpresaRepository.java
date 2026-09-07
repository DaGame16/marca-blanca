package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataEmpresaRepository extends JpaRepository<EmpresaDeAprovisionamientoEntity, Long> {

    boolean existsByIdentificador(String identificador);

    boolean existsByDominio(String dominio);

    Optional<EmpresaDeAprovisionamientoEntity> findByUuid(UUID uuid);

    /** El id serial interno a partir del uuid: lo necesitan el outbox y las tareas (FK BIGINT). */
    @Query("select e.id from EmpresaDeAprovisionamiento e where e.uuid = :uuid")
    Optional<Long> buscarIdInternoPorUuid(UUID uuid);
}