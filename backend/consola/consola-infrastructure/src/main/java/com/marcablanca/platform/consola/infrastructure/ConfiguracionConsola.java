package com.marcablanca.platform.consola.infrastructure;

import com.marcablanca.platform.consola.application.AutenticarOperadorService;
import com.marcablanca.platform.consola.application.CambiarContrasenaDeOperadorService;
import com.marcablanca.platform.consola.application.port.in.AutenticarOperador;
import com.marcablanca.platform.consola.application.port.in.CambiarContrasenaDeOperador;
import com.marcablanca.platform.consola.application.port.out.CifradorDeContrasenaDeOperador;
import com.marcablanca.platform.consola.application.port.out.EmisorDeTokenDeOperador;
import com.marcablanca.platform.consola.application.port.out.RepositorioOperadores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no llevan anotaciones de Spring (quedan libres de
 * framework). Esta clase los conecta como beans; el de cambio de contrasena se
 * envuelve en un decorador @Transactional que vive en infraestructura.
 */
@Configuration
public class ConfiguracionConsola {

    @Bean
    public AutenticarOperador autenticarOperador(RepositorioOperadores repositorioOperadores,
                                                 CifradorDeContrasenaDeOperador cifrador,
                                                 EmisorDeTokenDeOperador emisorDeToken) {
        return new AutenticarOperadorService(repositorioOperadores, cifrador, emisorDeToken);
    }

    @Bean
    public CambiarContrasenaDeOperador cambiarContrasenaDeOperador(RepositorioOperadores repositorioOperadores,
                                                                  CifradorDeContrasenaDeOperador cifrador) {
        return new CambiarContrasenaDeOperadorTransaccional(
                new CambiarContrasenaDeOperadorService(repositorioOperadores, cifrador));
    }
}
