package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.ResolverConexionDeEmpresaService;
import com.marcablanca.platform.empresas.application.port.in.ResolverConexionDeEmpresa;
import com.marcablanca.platform.empresas.application.port.out.RepositorioEmpresaConexiones;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El servicio de aplicacion no tiene anotaciones de Spring a proposito
 * (queda libre de framework) -- esta clase es la que lo conecta como bean.
 */
@Configuration
public class ConfiguracionEmpresas {

    @Bean
    public ResolverConexionDeEmpresa resolverConexionDeEmpresa(
            RepositorioEmpresaConexiones repositorioEmpresaConexiones) {
        return new ResolverConexionDeEmpresaService(repositorioEmpresaConexiones);
    }
}
