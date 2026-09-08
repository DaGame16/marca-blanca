package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

interface ConversacionJpaRepository extends JpaRepository<ConversacionEntity, Long> {

    Optional<ConversacionEntity> findByIdContacto(String idContacto);

    @Query("select c from ConversacionEntity c where "
            + "(:idContacto is null or c.idContacto = :idContacto) and "
            + "(:desde is null or c.creadoEn >= :desde) and (:hasta is null or c.creadoEn <= :hasta) and "
            + "exists (select 1 from CasoEntity cs where cs.conversacionId = c.id) "
            + "order by c.creadoEn desc")
    Page<ConversacionEntity> buscarRecientes(@Param("idContacto") String idContacto,
                                              @Param("desde") OffsetDateTime desde,
                                              @Param("hasta") OffsetDateTime hasta, Pageable pageable);
}
