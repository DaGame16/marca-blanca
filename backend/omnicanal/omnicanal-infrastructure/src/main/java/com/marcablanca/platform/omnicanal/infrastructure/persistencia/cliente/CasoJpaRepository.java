package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public interface CasoJpaRepository extends JpaRepository<CasoEntity, Long> {

    List<CasoEntity> findByConversacionIdOrderByArchivadaEnDesc(Long conversacionId);

    @Modifying
    @Transactional(transactionManager = "clienteTransactionManager")
    @Query("update CasoEntity c set c.esDeAds = :esDeAds where c.id = :id")
    void marcarEsDeAds(@Param("id") Long id, @Param("esDeAds") boolean esDeAds);

    /** Proyeccion para la serie temporal de estadisticas. */
    interface FilaSerieTemporal {
        String getPeriodo();

        long getTotal();
    }

    @Query(nativeQuery = true, value = """
            select to_char(c.archivada_en, :formato) as periodo, count(*) as total
            from omnicanal.tbl_casos_liwa c
            where c.archivada_en >= :desde and c.archivada_en <= :hasta
            group by periodo
            order by periodo asc
            """)
    List<FilaSerieTemporal> serieTemporal(@Param("formato") String formato,
                                          @Param("desde") OffsetDateTime desde,
                                          @Param("hasta") OffsetDateTime hasta);

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
