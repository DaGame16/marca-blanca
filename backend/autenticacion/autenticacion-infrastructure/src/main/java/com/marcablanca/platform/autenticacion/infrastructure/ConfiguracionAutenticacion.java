package com.marcablanca.platform.autenticacion.infrastructure;

import com.marcablanca.platform.autenticacion.application.AutenticarUsuarioService;
import com.marcablanca.platform.autenticacion.application.RenovarTokenService;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeUsuarios;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * (quedan libres de framework) -- esta clase es la que los conecta como beans.
 */
@Configuration
public class ConfiguracionAutenticacion {

    @Bean
    public AutenticarUsuario autenticarUsuario(
            VerificadorDeUsuarios verificadorDeUsuarios,
            GeneradorDeToken generadorDeToken,
            AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco) {
        return new AutenticarUsuarioService(verificadorDeUsuarios, generadorDeToken, almacenDeTokensDeRefresco);
    }

    @Bean
    public RenovarToken renovarToken(
            AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco,
            GeneradorDeToken generadorDeToken,
            VerificadorDeUsuarios verificadorDeUsuarios) {
        return new RenovarTokenService(almacenDeTokensDeRefresco, generadorDeToken, verificadorDeUsuarios);
    }
}
