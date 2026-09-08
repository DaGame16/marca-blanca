package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.application.*;
import com.marcablanca.platform.omnicanal.application.port.in.*;
import com.marcablanca.platform.omnicanal.application.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * -- esta clase los conecta como beans, mismo patron que el resto del proyecto.
 */
@Configuration
public class ConfiguracionOmnicanal {

    @Bean
    public RepositorioAnalisisEscritor repositorioAnalisisEscritor(RepositorioAnalisis repositorioAnalisis) {
        return new RepositorioAnalisisEscritor(repositorioAnalisis);
    }

    @Bean
    public RecibirConversacionArchivada recibirConversacionArchivada(
            ResolverEmpresaPorWebhookSecreto resolverEmpresa, RepositorioConversaciones repositorioConversaciones,
            RepositorioCasos repositorioCasos, AnalizadorDeConversacion analizadorDeConversacion,
            RepositorioAnalisisEscritor escritorAnalisis) {
        return new RecibirConversacionArchivadaService(resolverEmpresa, repositorioConversaciones, repositorioCasos,
                analizadorDeConversacion, escritorAnalisis);
    }

    @Bean
    public ConsultarConversaciones consultarConversaciones(RepositorioConversaciones repositorioConversaciones) {
        return new ConsultarConversacionesService(repositorioConversaciones);
    }

    @Bean
    public ConsultarAnalisisDeCasos consultarAnalisisDeCasos(RepositorioAnalisis repositorioAnalisis,
                                                              RepositorioConversaciones repositorioConversaciones) {
        return new ConsultarAnalisisDeCasosService(repositorioAnalisis, repositorioConversaciones);
    }

    @Bean
    public ConsultarReportesOmnicanal consultarReportesOmnicanal(RepositorioAnalisis repositorioAnalisis,
                                                                   RepositorioCasos repositorioCasos) {
        return new ConsultarReportesOmnicanalService(repositorioAnalisis, repositorioCasos);
    }

    @Bean
    public GestionarReprocesamiento gestionarReprocesamiento(RepositorioCasos repositorioCasos,
            RepositorioConversaciones repositorioConversaciones, AnalizadorDeConversacion analizadorDeConversacion,
            RepositorioAnalisisEscritor escritorAnalisis) {
        return new GestionarReprocesamientoService(repositorioCasos, repositorioConversaciones,
                analizadorDeConversacion, escritorAnalisis);
    }
}
