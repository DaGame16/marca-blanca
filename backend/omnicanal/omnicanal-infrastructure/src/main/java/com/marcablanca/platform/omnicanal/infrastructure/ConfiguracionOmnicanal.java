package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.application.*;
import com.marcablanca.platform.omnicanal.application.port.in.*;
import com.marcablanca.platform.omnicanal.application.port.out.*;
import com.marcablanca.platform.omnicanal.infrastructure.realtime.NotificadorEventosOmnicanalStomp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * -- esta clase los conecta como beans, mismo patron que el resto del proyecto.
 */
@Configuration
public class ConfiguracionOmnicanal {

    @Bean
    public NotificadorEventosOmnicanal notificadorEventosOmnicanal(SimpMessagingTemplate mensajeria) {
        return new NotificadorEventosOmnicanalStomp(mensajeria);
    }

    @Bean
    public RepositorioAnalisisEscritor repositorioAnalisisEscritor(RepositorioAnalisis repositorioAnalisis,
            RepositorioConfiguracionOmnicanal configuracionOmnicanal, NotificadorEventosOmnicanal notificador) {
        return new RepositorioAnalisisEscritor(repositorioAnalisis, configuracionOmnicanal, notificador);
    }

    /**
     * Solo las escrituras de la ingesta van en transaccion (base del cliente);
     * el analisis IA que dispara despues RecibirConversacionArchivadaService
     * queda FUERA, para no tener una conexion tomada mientras espera a OpenAI.
     */
    @Bean
    public IngestarConversacionArchivada ingestarConversacionArchivada(
            RepositorioConversaciones repositorioConversaciones, RepositorioCasos repositorioCasos,
            RepositorioConfiguracionOmnicanal configuracionOmnicanal) {
        return new IngestarConversacionArchivadaTransaccional(
                new IngestarConversacionArchivadaService(repositorioConversaciones, repositorioCasos,
                        configuracionOmnicanal));
    }

    @Bean
    public RecibirConversacionArchivada recibirConversacionArchivada(
            IngestarConversacionArchivada ingestarConversacionArchivada,
            AnalizadorDeConversacion analizadorDeConversacion, RepositorioAnalisisEscritor escritorAnalisis,
            RepositorioConversaciones repositorioConversaciones, RepositorioCasos repositorioCasos,
            NotificadorEventosOmnicanal notificador) {
        return new RecibirConversacionArchivadaService(ingestarConversacionArchivada, analizadorDeConversacion,
                escritorAnalisis, repositorioConversaciones, repositorioCasos, notificador);
    }

    @Bean
    public ConsultarConversaciones consultarConversaciones(RepositorioConversaciones repositorioConversaciones,
            RepositorioCasos repositorioCasos, RepositorioAnalisis repositorioAnalisis) {
        return new ConsultarConversacionesService(repositorioConversaciones, repositorioCasos, repositorioAnalisis);
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
    public ConfigurarOmnicanal configurarOmnicanal(RepositorioConfiguracionOmnicanal configuracionOmnicanal,
            RegistroRuteoOmnicanal registroRuteoOmnicanal,
            @Value("${app.omnicanal.webhook-url-base:http://localhost:8080}") String webhookUrlBase) {
        return new ConfigurarOmnicanalService(configuracionOmnicanal, registroRuteoOmnicanal, webhookUrlBase);
    }

    @Bean
    public EjecutarBackfillDeAds ejecutarBackfillDeAds(RepositorioConversaciones repositorioConversaciones,
            RepositorioCasos repositorioCasos, RepositorioAnalisis repositorioAnalisis, ClienteLiwa clienteLiwa) {
        return new EjecutarBackfillDeAdsService(repositorioConversaciones, repositorioCasos, repositorioAnalisis,
                clienteLiwa);
    }

    @Bean
    public GestionarReprocesamiento gestionarReprocesamiento(RepositorioCasos repositorioCasos,
            RepositorioConversaciones repositorioConversaciones, AnalizadorDeConversacion analizadorDeConversacion,
            RepositorioAnalisisEscritor escritorAnalisis) {
        return new GestionarReprocesamientoService(repositorioCasos, repositorioConversaciones,
                analizadorDeConversacion, escritorAnalisis);
    }
}
