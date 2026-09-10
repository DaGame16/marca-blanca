package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registra FiltroTenantOmnicanalWebhook SOLO para el path del webhook. Se
 * registra explicito (y el filtro NO lleva @Component) para que no se
 * convierta en un filtro global: la resolucion de tenant por secreto solo
 * tiene sentido en /api/v1/omnicanal/webhook/**; el resto de omnicanal
 * resuelve el tenant por JWT (JwtAuthFilter).
 */
@Configuration
class ConfiguracionWebOmnicanal {

    @Bean
    FilterRegistrationBean<FiltroTenantOmnicanalWebhook> filtroTenantOmnicanalWebhook(
            ResolverEmpresaPorWebhookSecreto resolverEmpresa) {
        var registro = new FilterRegistrationBean<>(new FiltroTenantOmnicanalWebhook(resolverEmpresa));
        registro.addUrlPatterns("/api/v1/omnicanal/webhook/*");
        registro.setOrder(Ordered.LOWEST_PRECEDENCE);
        registro.setName("filtroTenantOmnicanalWebhook");
        return registro;
    }
}
