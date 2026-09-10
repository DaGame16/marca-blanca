package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

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

public interface ConversacionJpaRepository extends JpaRepository<ConversacionEntity, Long> {

    Optional<ConversacionEntity> findByIdContacto(String idContacto);

    @Query("select c from ConversacionEntity c where "
            + "(:idContacto is null or c.idContacto = :idContacto) and "
            + "(:desde is null or c.creadoEn >= :desde) and (:hasta is null or c.creadoEn <= :hasta) and "
            + "exists (select 1 from CasoEntity cs where cs.conversacionId = c.id) "
            + "order by c.creadoEn desc")
    Page<ConversacionEntity> buscarRecientes(@Param("idContacto") String idContacto,
                                              @Param("desde") OffsetDateTime desde,
                                              @Param("hasta") OffsetDateTime hasta, Pageable pageable);

    /** (id, idContacto) de cada conversacion -- para el backfill de ads (1 fila por contacto). */
    interface RefContactoProj {
        Long getId();

        String getIdContacto();
    }

    @Query("select c.id as id, c.idContacto as idContacto from ConversacionEntity c")
    List<RefContactoProj> contactos();

    @Modifying
    @Transactional(transactionManager = "clienteTransactionManager")
    @Query("update ConversacionEntity c set c.esDeAds = :vieneDeAds where c.id = :id")
    void marcarVieneDeAds(@Param("id") Long id, @Param("vieneDeAds") boolean vieneDeAds);
}
