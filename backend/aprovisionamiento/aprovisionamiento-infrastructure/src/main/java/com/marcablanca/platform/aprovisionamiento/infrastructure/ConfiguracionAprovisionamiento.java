package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.RegistrarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.SeleccionarModuloService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.SeleccionarModulo;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * (quedan libres de framework) -- esta clase los conecta como beans.
 */
@Configuration
public class ConfiguracionAprovisionamiento {

    @Bean
    RegistrarEmpresa registrarEmpresa(
            RepositorioEmpresas repositorioEmpresas,
            @Value("${app.aprovisionamiento.sufijo-dominio:mb}") String sufijoDominio) {
        return new RegistrarEmpresaTransaccional(
                new RegistrarEmpresaService(repositorioEmpresas, sufijoDominio));
    }

    @Bean
    SeleccionarModulo seleccionarModulo(RepositorioEmpresas repositorioEmpresas,
                                        ActivadorDeModulosDeEmpresa modulos) {
        return new SeleccionarModuloService(repositorioEmpresas, modulos);
    }
}