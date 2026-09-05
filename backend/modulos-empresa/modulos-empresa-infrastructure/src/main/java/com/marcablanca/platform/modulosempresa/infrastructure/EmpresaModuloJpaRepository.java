package com.marcablanca.platform.modulosempresa.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaModuloJpaRepository extends JpaRepository<EmpresaModuloEntity, Long> {

    Optional<EmpresaModuloEntity> findByEmpresaIdAndModuloId(Long empresaId, Long moduloId);

    @Query("select e.id from EmpresaRefDeModulosEmpresa e where e.uuid = :empresaUuid")
    Optional<Long> buscarEmpresaIdInternoPorUuid(@Param("empresaUuid") UUID empresaUuid);

    @Query("select m.codigo from EmpresaModuloEntity em join ModuloEntity m on m.id = em.moduloId "
            + "where em.empresaId = :empresaIdInterno and em.esActivo = true")
    List<String> listarCodigosActivosDeEmpresa(@Param("empresaIdInterno") Long empresaIdInterno);
}
