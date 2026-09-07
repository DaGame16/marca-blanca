package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.RegistrarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.CifradorDeContrasenaMaestra;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RegistroDeEventos;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * (quedan libres de framework) -- esta clase los conecta como beans.
 * EjecutarAprovisionamiento se cablea en el bloque 6 (necesita PasosDeAprovisionamiento).
 */
@Configuration
public class ConfiguracionAprovisionamiento {

    @Bean
    RegistrarEmpresa registrarEmpresa(RepositorioEmpresas repositorioEmpresas,
                                      RegistroDeEventos registroDeEventos,
                                      CifradorDeContrasenaMaestra cifrador) {
        return new RegistrarEmpresaTransaccional(
                new RegistrarEmpresaService(repositorioEmpresas, registroDeEventos, cifrador));
    }
}
