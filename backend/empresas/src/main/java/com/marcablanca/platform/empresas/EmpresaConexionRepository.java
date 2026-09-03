package com.marcablanca.platform.empresas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Resuelve, a partir del identificador de empresa que llega en el login
 * (ver ADR 0001 del modulo usuarios), la conexion activa a la base de
 * datos de esa empresa.
 */
public interface EmpresaConexionRepository extends JpaRepository<EmpresaConexionEntity, Long> {

    @Query("""
        select c from EmpresaConexionEntity c
        where c.empresaId = (
            select e.id from EmpresaEntity e
            where e.identificador = :identificadorEmpresa
              and e.estado = 'activa'
        )
        and c.esActiva = true
        """)
    Optional<EmpresaConexionEntity> buscarConexionActivaPorIdentificador(
            @Param("identificadorEmpresa") String identificadorEmpresa);
}
