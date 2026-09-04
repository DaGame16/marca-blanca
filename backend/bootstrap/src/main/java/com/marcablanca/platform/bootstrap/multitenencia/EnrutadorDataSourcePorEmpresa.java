package com.marcablanca.platform.bootstrap.multitenencia;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.empresas.application.port.in.ResolverConexionDeEmpresa;
import com.marcablanca.platform.empresas.domain.EmpresaConexion;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Enruta cada consulta a la base de datos fisica de la empresa activa en
 * ContextoEmpresaActual. Las conexiones se resuelven vía el puerto
 * ResolverConexionDeEmpresa (modulo empresas) y se cachean en memoria --
 * una unica vez por empresa, no en cada consulta.
 *
 * NOTA DEV: usa las credenciales de rol conocidas (guajiranet_app), no
 * resuelve "secreto_ref" contra ningun vault -- eso es tarea de
 * infraestructura para QA/PROD (ver README de base de datos, seccion 10).
 *
 * NOTA TECNICA: AbstractRoutingDataSource, por defecto, exige una lista
 * fija de datasources conocida de antemano. Como las empresas se crean
 * sobre la marcha, esta clase resuelve y cachea las conexiones de forma
 * dinamica en determineTargetDataSource(), y por eso sobreescribe
 * afterPropertiesSet() para saltarse esa exigencia -- no es un olvido.
 */
public class EnrutadorDataSourcePorEmpresa extends AbstractRoutingDataSource {

    private final ResolverConexionDeEmpresa resolverConexionDeEmpresa;
    private final ConcurrentMap<String, DataSource> cache = new ConcurrentHashMap<>();

    public EnrutadorDataSourcePorEmpresa(ResolverConexionDeEmpresa resolverConexionDeEmpresa) {
        this.resolverConexionDeEmpresa = resolverConexionDeEmpresa;
    }

    @Override
    public void afterPropertiesSet() {
        // Intencional -- ver nota tecnica en el comentario de la clase.
    }

    @Override
    protected Object determineCurrentLookupKey() {
        // No se usa en la practica -- determineTargetDataSource() esta
        // sobreescrito abajo y no llama a este metodo. Existe solo porque
        // la clase base lo declara abstracto y Java exige implementarlo.
        return ContextoEmpresaActual.obtener().orElse(null);
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String identificadorEmpresa = ContextoEmpresaActual.obtener()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay empresa activa en el contexto de la peticion"));

        return cache.computeIfAbsent(identificadorEmpresa, this::crearDataSourcePara);
    }

    private DataSource crearDataSourcePara(String identificadorEmpresa) {
        EmpresaConexion conexion = resolverConexionDeEmpresa.ejecutar(identificadorEmpresa);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://" + conexion.host() + ":" + conexion.puerto()
                + "/" + conexion.nombreBd());
        dataSource.setUsername("guajiranet_app");
        dataSource.setPassword("guajiranet_app");
        dataSource.setPoolName("tenant-" + identificadorEmpresa);
        return dataSource;
    }
}
