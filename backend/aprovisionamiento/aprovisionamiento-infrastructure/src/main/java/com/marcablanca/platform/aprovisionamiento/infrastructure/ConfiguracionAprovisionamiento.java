package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.CambiarEstadoDeEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.FinalizarRegistroService;
import com.marcablanca.platform.aprovisionamiento.application.ListarEmpresasService;
import com.marcablanca.platform.aprovisionamiento.application.PersonalizarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.RegistrarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.SeleccionarModuloService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.CambiarEstadoDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.FinalizarRegistro;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ListarEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.in.PersonalizarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.SeleccionarModulo;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ConsultaDeEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RegistroDeEventos;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioPersonalizacion;
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

    @Bean
    PersonalizarEmpresa personalizarEmpresa(RepositorioEmpresas repositorioEmpresas,
                                            RepositorioPersonalizacion repositorioPersonalizacion) {
        return new PersonalizarEmpresaService(repositorioEmpresas, repositorioPersonalizacion);
    }

    @Bean
    FinalizarRegistro finalizarRegistro(RepositorioEmpresas repositorioEmpresas,
                                        RegistroDeEventos registroDeEventos,
                                        ActivadorDeModulosDeEmpresa modulos) {
        return new FinalizarRegistroTransaccional(
                new FinalizarRegistroService(repositorioEmpresas, registroDeEventos, modulos));
    }

    @Bean
    ListarEmpresas listarEmpresas(ConsultaDeEmpresas consultaDeEmpresas) {
        return new ListarEmpresasService(consultaDeEmpresas);
    }

    @Bean
    CambiarEstadoDeEmpresa cambiarEstadoDeEmpresa(RepositorioEmpresas repositorioEmpresas) {
        return new CambiarEstadoDeEmpresaTransaccional(
                new CambiarEstadoDeEmpresaService(repositorioEmpresas));
    }
}