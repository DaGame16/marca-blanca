package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEventoSalienteRepository extends JpaRepository<EventoSalienteEntity, Long> {
    // El consumo (poll con SKIP LOCKED) se agrega en el bloque 6, con @Query nativa.
}