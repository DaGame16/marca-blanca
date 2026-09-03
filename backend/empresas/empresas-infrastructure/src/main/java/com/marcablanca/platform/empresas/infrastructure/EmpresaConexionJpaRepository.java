package com.marcablanca.platform.empresas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Detalle de implementacion, package-private a proposito -- solo lo usa
 * RepositorioEmpresaConexionesJpa, que es quien expone el puerto real
 * hacia el resto de la aplicacion.
 */
interface EmpresaConexionJpaRepository extends JpaRepository<EmpresaConexionEntity, Long> {

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
