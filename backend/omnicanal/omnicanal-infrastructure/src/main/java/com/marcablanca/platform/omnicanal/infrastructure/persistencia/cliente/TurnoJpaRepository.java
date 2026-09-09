package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long> {

    List<TurnoEntity> findByConversacionIdOrderByOrdenAsc(Long conversacionId);

    @Query("select coalesce(max(t.orden), -1) + 1 from TurnoEntity t where t.conversacionId = :conversacionId")
    int siguienteOrden(@Param("conversacionId") Long conversacionId);
}
