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
 * Repositorios de la base de CONTROL (modulos empresas + identidad-visual +
 * modulos-empresa). Usa "entityManagerFactory" y "transactionManager" --
 * nombres de bean por defecto que Spring Boot auto-configura con
 * spring.datasource.*.
 *
 * Esta clase existe SOLO porque ConfiguracionPersistenciaCliente agrega su
 * propio @EnableJpaRepositories -- y en cuanto aparece CUALQUIER
 * @EnableJpaRepositories explicito en la app, Spring Boot apaga la
 * deteccion automatica de repositorios para TODA la aplicacion.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.marcablanca.platform.empresas.infrastructure",
                "com.marcablanca.platform.identidadvisual.infrastructure",
                "com.marcablanca.platform.modulosempresa.infrastructure"
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class ConfiguracionPersistenciaControl {

    /**
     * Declarada explicita y marcada @Primary a proposito: al agregar un
     * segundo DataSource (el enrutador multi-tenant), Spring deja de saber
     * cual es "el" DataSource por defecto.
     *
     * Se arma en dos pasos (Properties + build) porque pegarle las
     * propiedades directo a un DataSource ya construido no funciona --
     * Hikari usa "jdbcUrl", no "url", y DataSourceProperties es quien sabe
     * traducir eso correctamente segun el pool que se use.
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

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("controlDataSource") DataSource controlDataSource) {
        return builder
                .dataSource(controlDataSource)
                .packages("com.marcablanca.platform.empresas.infrastructure",
                        "com.marcablanca.platform.identidadvisual.infrastructure",
                        "com.marcablanca.platform.modulosempresa.infrastructure")
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
