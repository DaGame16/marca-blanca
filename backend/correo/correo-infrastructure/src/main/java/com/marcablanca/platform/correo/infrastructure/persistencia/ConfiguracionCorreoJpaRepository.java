package com.marcablanca.platform.correo.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

// public a proposito -- Spring Boot DevTools usa 2 classloaders para el hot-reload,
// y un proxy dinamico de una interfaz no publica no se puede crear entre los dos
// (ya nos paso este mismo bug varias veces en el proyecto).
public interface ConfiguracionCorreoJpaRepository extends JpaRepository<ConfiguracionCorreoEntity, Long> {

    Optional<ConfiguracionCorreoEntity> findByUuid(UUID uuid);

    @Modifying
    @Query("update ConfiguracionCorreoEntity c set c.esActiva = false where c.uuid <> :uuid")
    void desactivarTodasMenos(@Param("uuid") UUID uuid);

    void deleteByUuid(UUID uuid);
}
