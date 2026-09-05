package com.marcablanca.platform.identidadvisual.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaMarcaJpaRepository extends JpaRepository<EmpresaMarcaEntity, Long> {

    Optional<EmpresaMarcaEntity> findByEmpresaId(Long empresaId);

    @Query("select e.id from EmpresaRefDeIdentidadVisual e where e.identificador = :identificadorEmpresa and e.estado = 'activa'")
    Optional<Long> buscarEmpresaIdInternoPorIdentificador(@Param("identificadorEmpresa") String identificadorEmpresa);
}
