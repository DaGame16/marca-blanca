package com.marcablanca.platform.usuarios.infrastructure;

import com.marcablanca.platform.usuarios.application.GestionarUsuarioService;
import com.marcablanca.platform.usuarios.application.port.in.GestionarUsuario;
import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarioPerfiles;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionUsuarios {

    @Bean
    public GestionarUsuario gestionarUsuario(RepositorioUsuarios repositorioUsuarios,
                                              RepositorioUsuarioPerfiles repositorioUsuarioPerfiles,
                                              CifradorDeContrasenas cifradorDeContrasenas) {
        return new GestionarUsuarioService(repositorioUsuarios, repositorioUsuarioPerfiles, cifradorDeContrasenas);
    }
}