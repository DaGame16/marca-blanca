package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.domain.AbandonadoPor;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AnalisisJpaRepository extends JpaRepository<AnalisisEntity, Long> {

    Optional<AnalisisEntity> findByUuid(java.util.UUID uuid);

    Optional<AnalisisEntity> findByCasoId(Long casoId);

    void deleteByCasoId(Long casoId);

    @Modifying
    @Transactional(transactionManager = "clienteTransactionManager")
    @Query("update AnalisisEntity a set a.esDeAds = :esDeAds where a.casoId = :casoId")
    void marcarEsDeAdsPorCaso(@Param("casoId") Long casoId, @Param("esDeAds") boolean esDeAds);

    @Query("select a from AnalisisEntity a where "
            + "(:resultado is null or a.resultado = :resultado) and "
            + "(:motivoContacto is null or a.motivoContacto = :motivoContacto) and "
            + "(:abandono is null or a.abandono = :abandono) and "
            + "(:abandonadoPor is null or a.abandonadoPor = :abandonadoPor) and "
            + "(:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta) "
            + "order by a.procesadoEn desc")
    Page<AnalisisEntity> buscar(@Param("resultado") Resultado resultado, @Param("motivoContacto") String motivoContacto,
                                 @Param("abandono") Boolean abandono, @Param("abandonadoPor") AbandonadoPor abandonadoPor,
                                 @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                 Pageable pageable);

    @Query("select a.sentimientoInicial as clave, count(a) as total from AnalisisEntity a "
            + "where (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta) "
            + "group by a.sentimientoInicial")
    List<Object[]> agruparPorSentimientoInicial(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select a.sentimientoFinal as clave, count(a) as total from AnalisisEntity a "
            + "where (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta) "
            + "group by a.sentimientoFinal")
    List<Object[]> agruparPorSentimientoFinal(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(a) from AnalisisEntity a where a.motivoContacto = :motivo and a.resultado = :resultado "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta)")
    long contarPorMotivoYResultado(@Param("motivo") String motivo, @Param("resultado") Resultado resultado,
                                   @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(a) from AnalisisEntity a where a.motivoContacto = :motivo "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta)")
    long contarPorMotivo(@Param("motivo") String motivo,
                         @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(a) from AnalisisEntity a where a.oportunidadVenta = true "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta)")
    long contarOportunidadesDeVenta(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(a) from AnalisisEntity a where a.oportunidadVenta = true and a.ventaConfirmadaEnTexto = true "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta)")
    long contarVentasConfirmadasEnTexto(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select count(a) from AnalisisEntity a where a.esDeAds = true "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta)")
    long contarConAds(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select a.motivoContacto as clave, count(a) as total from AnalisisEntity a where a.esDeAds = true "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta) "
            + "group by a.motivoContacto")
    List<Object[]> agruparPorMotivoConAds(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("select a.resultado as clave, count(a) as total from AnalisisEntity a where a.esDeAds = true "
            + "and (:desde is null or a.procesadoEn >= :desde) and (:hasta is null or a.procesadoEn <= :hasta) "
            + "group by a.resultado")
    List<Object[]> agruparPorResultadoConAds(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
