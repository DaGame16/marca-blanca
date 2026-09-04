package com.marcablanca.platform.autenticacion.infrastructure;

import com.marcablanca.platform.autenticacion.application.AutenticarUsuarioService;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AutenticarUsuarioService no tiene anotaciones de Spring a proposito
 * (queda libre de framework) -- esta clase es la que lo conecta como bean.
 * Mismo patron que ConfiguracionEmpresas en el modulo empresas.
 */
@Configuration
public class ConfiguracionAutenticacion {

    @Bean
    public AutenticarUsuario autenticarUsuario(
            RepositorioUsuarios repositorioUsuarios,
            CifradorDeContrasenas cifradorDeContrasenas,
            GeneradorDeToken generadorDeToken) {
        return new AutenticarUsuarioService(repositorioUsuarios, cifradorDeContrasenas, generadorDeToken);
    }
}