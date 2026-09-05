package com.marcablanca.platform.bootstrap.multitenencia;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Repositorios de la base de CONTROL (modulo empresas).
 *
 * Esta clase existe SOLO porque ConfiguracionPersistenciaCliente (en este mismo
 * paquete) agrega su propio @EnableJpaRepositories -- y en cuanto aparece
 * CUALQUIER @EnableJpaRepositories explicito en la app, Spring Boot apaga la
 * deteccion automatica de repositorios para TODA la aplicacion, no solo para
 * el modulo nuevo. Sin esta clase, EmpresaConexionJpaRepository dejaria de
 * funcionar.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.marcablanca.platform.empresas.infrastructure",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class ConfiguracionPersistenciaControl {

    /**
     * Declarada explicita y marcada @Primary a proposito: al agregar un
     * segundo DataSource (el enrutador multi-tenant), Spring deja de saber
     * cual es "el" DataSource por defecto. Sin esto, arrancar la app termina
     * en un enredo circular -- Liquibase, al intentar resolver cual DataSource
     * usar, termina disparando la construccion del enrutador, que a su vez
     * depende de esta misma base de control para funcionar.
     *
     * Se arma en dos pasos (Properties + build) porque pegarle las propiedades
     * directo a un DataSource ya construido no funciona -- Hikari usa
     * "jdbcUrl", no "url", y DataSourceProperties es quien sabe traducir eso
     * correctamente segun el pool que se use.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties controlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource controlDataSource(DataSourceProperties controlDataSourceProperties) {
        return controlDataSourceProperties.initializeDataSourceBuilder().build();
    }

    /**
     * Declarado a mano, no lo auto-configura Spring Boot: en cuanto la app
     * tiene CUALQUIER OTRO bean de tipo EntityManagerFactory (el de "cliente"),
     * Spring Boot deja de crear el suyo por defecto -- la condicion que usa
     * es por tipo, no por nombre. Por eso hay que declarar este tambien a mano,
     * o el nombre "entityManagerFactory" nunca llega a existir.
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("controlDataSource") DataSource controlDataSource) {
        return builder
                .dataSource(controlDataSource)
                .packages("com.marcablanca.platform.empresas.infrastructure")
                .persistenceUnit("control")
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}