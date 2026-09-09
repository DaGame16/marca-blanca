package com.marcablanca.platform.empresas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Detalle de implementacion, package-private a proposito -- solo lo usa
 * RepositorioEmpresaConexionesJpa, que es quien expone el puerto real
 * hacia el resto de la aplicacion.
 */
public interface EmpresaConexionJpaRepository extends JpaRepository<EmpresaConexionEntity, Long> {

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

    @Query("""
        select e.identificador as identificador, c.host as host, c.puerto as puerto, c.nombreBd as nombreBd
        from EmpresaConexionEntity c
        join EmpresaEntity e on e.id = c.empresaId
        where e.estado = 'activa' and c.esActiva = true
        """)
    List<ConexionActivaProyeccion> listarConexionesActivas();

    interface ConexionActivaProyeccion {
        String getIdentificador();

        String getHost();

        Integer getPuerto();

        String getNombreBd();
    }
}
