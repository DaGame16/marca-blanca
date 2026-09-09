package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface CasoJpaRepository extends JpaRepository<CasoEntity, Long> {

    @Query("select c from CasoEntity c where c.esProcesada = false and "
            + "(:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    List<CasoEntity> listarPendientes(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select c from CasoEntity c where "
            + "(c.esProcesada = false or not exists (select 1 from AnalisisEntity a where a.casoId = c.id and a.razonamiento is not null)) and "
            + "(:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    List<CasoEntity> listarSinReanalizar(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(c) from CasoEntity c where c.esProcesada = false and "
            + "(:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    long contarPendientes(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(c) from CasoEntity c where "
            + "(c.esProcesada = false or not exists (select 1 from AnalisisEntity a where a.casoId = c.id and a.razonamiento is not null)) and "
            + "(:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    long contarSinReanalizar(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(c) from CasoEntity c where (:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    long contarTotal(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(c) from CasoEntity c where c.esProcesada = true and "
            + "exists (select 1 from AnalisisEntity a where a.casoId = c.id and a.razonamiento is not null) and "
            + "(:desde is null or c.archivadaEn >= :desde) and (:hasta is null or c.archivadaEn <= :hasta)")
    long contarConAnalisisNuevo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
