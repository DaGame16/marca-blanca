package com.marcablanca.platform.identidadvisual.infrastructure;

import com.marcablanca.platform.identidadvisual.application.ActualizarMarcaDeEmpresaService;
import com.marcablanca.platform.identidadvisual.application.ObtenerMarcaDeEmpresaService;
import com.marcablanca.platform.identidadvisual.application.port.in.ActualizarMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.application.port.in.ObtenerMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.application.port.out.RepositorioMarcaDeEmpresa;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionIdentidadVisual {

    @Bean
    public ObtenerMarcaDeEmpresa obtenerMarcaDeEmpresa(RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa) {
        return new ObtenerMarcaDeEmpresaService(repositorioMarcaDeEmpresa);
    }

    @Bean
    public ActualizarMarcaDeEmpresa actualizarMarcaDeEmpresa(RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa) {
        return new ActualizarMarcaDeEmpresaService(repositorioMarcaDeEmpresa);
    }
}
