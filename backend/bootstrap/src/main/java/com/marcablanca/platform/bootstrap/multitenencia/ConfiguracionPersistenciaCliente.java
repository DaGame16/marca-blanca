package com.marcablanca.platform.bootstrap.multitenencia;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.empresas.application.port.in.ResolverConexionDeEmpresa;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Segunda unidad de persistencia -- para las tablas que viven en la base
 * de CADA EMPRESA (schema "seguridad", etc.), no en la de control.
 *
 * Paquetes de "cliente" incluidos hasta ahora: usuarios (identidad) y
 * autenticacion (sesiones/refresh token). Al agregar mas adaptadores de
 * "cliente" en el futuro, hay que sumar su paquete tanto a basePackages
 * de @EnableJpaRepositories como al .packages(...) de abajo.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.marcablanca.platform.usuarios.infrastructure.persistencia",
                "com.marcablanca.platform.autenticacion.infrastructure.persistencia"
        },
        entityManagerFactoryRef = "clienteEntityManagerFactory",
        transactionManagerRef = "clienteTransactionManager"
)
public class ConfiguracionPersistenciaCliente {

    @Bean(name = "clienteRoutingDataSource")
    public DataSource clienteRoutingDataSource(@Lazy ResolverConexionDeEmpresa resolverConexionDeEmpresa) {
        return new EnrutadorDataSourcePorEmpresa(resolverConexionDeEmpresa);
    }

    @Bean(name = "clienteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean clienteEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("clienteRoutingDataSource") DataSource clienteRoutingDataSource) {
        return builder
                .dataSource(clienteRoutingDataSource)
                .packages("com.marcablanca.platform.usuarios.infrastructure.persistencia",
                        "com.marcablanca.platform.autenticacion.infrastructure.persistencia")
                .persistenceUnit("cliente")
                // Liquibase (aplicado a mano contra la plantilla) es quien garantiza
                // la estructura de cada base de cliente -- Hibernate no necesita
                // validarla de nuevo, y no podria hacerlo al arrancar de todas
                // formas: todavia no hay ninguna empresa activa en ese momento.
                .properties(Map.of("hibernate.hbm2ddl.auto", "none"))
                .build();
    }

    @Bean(name = "clienteTransactionManager")
    public PlatformTransactionManager clienteTransactionManager(
            @Qualifier("clienteEntityManagerFactory") EntityManagerFactory clienteEntityManagerFactory) {
        return new JpaTransactionManager(clienteEntityManagerFactory);
    }
}
