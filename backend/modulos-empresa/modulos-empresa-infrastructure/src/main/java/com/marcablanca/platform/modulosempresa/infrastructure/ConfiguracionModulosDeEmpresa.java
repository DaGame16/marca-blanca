package com.marcablanca.platform.modulosempresa.infrastructure;

import com.marcablanca.platform.modulosempresa.application.ActivarModuloDeEmpresaService;
import com.marcablanca.platform.modulosempresa.application.DesactivarModuloDeEmpresaService;
import com.marcablanca.platform.modulosempresa.application.ListarModulosDeEmpresaService;
import com.marcablanca.platform.modulosempresa.application.ListarModulosService;
import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.DesactivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulos;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulos;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulosDeEmpresa;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * (quedan libres de framework) -- esta clase es la que los conecta como beans.
 */
@Configuration
public class ConfiguracionModulosDeEmpresa {

    @Bean
    public ListarModulos listarModulos(RepositorioModulos repositorioModulos) {
        return new ListarModulosService(repositorioModulos);
    }

    @Bean
    public ListarModulosDeEmpresa listarModulosDeEmpresa(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        return new ListarModulosDeEmpresaService(repositorioModulosDeEmpresa);
    }

    @Bean
    public ActivarModuloDeEmpresa activarModuloDeEmpresa(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        return new ActivarModuloDeEmpresaService(repositorioModulosDeEmpresa);
    }

    @Bean
    public DesactivarModuloDeEmpresa desactivarModuloDeEmpresa(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        return new DesactivarModuloDeEmpresaService(repositorioModulosDeEmpresa);
    }
}
