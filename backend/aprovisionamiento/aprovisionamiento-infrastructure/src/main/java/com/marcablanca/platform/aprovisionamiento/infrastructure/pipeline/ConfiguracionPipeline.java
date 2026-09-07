package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.EjecutarAprovisionamientoService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.EjecutarAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.application.port.out.PasosDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioTareasDeAprovisionamiento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * Capa 2. Cablea el pipeline y su sondeador. @EnableScheduling activa el @Scheduled
 * de SondeadorDeEventos.
 */
@Configuration
@EnableScheduling
class ConfiguracionPipeline {

    /**
     * Conexion de MANTENIMIENTO con rol owner, apuntando a la base 'postgres'
     * (nunca a una base de cliente). Es la unica que ejecuta DDL: CREATE DATABASE
     * no admite transaccion, por eso va en un DataSource aparte y no en las
     * unidades de persistencia JPA. En QA/PROD la credencial sale de un secreto.
     */
    @Bean
    DataSource dataSourceMantenimiento(
            @Value("${app.aprovisionamiento.mantenimiento.url:jdbc:postgresql://localhost:5432/postgres}") String url,
            @Value("${app.aprovisionamiento.mantenimiento.username:guajiranet_owner}") String usuario,
            @Value("${app.aprovisionamiento.mantenimiento.password:guajiranet_owner}") String clave) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, usuario, clave);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    @Bean
    EjecutarAprovisionamiento ejecutarAprovisionamiento(RepositorioEmpresas repositorioEmpresas,
                                                        RepositorioTareasDeAprovisionamiento repositorioTareas,
                                                        PasosDeAprovisionamiento pasos) {
        return new EjecutarAprovisionamientoService(repositorioEmpresas, repositorioTareas, pasos);
    }
}