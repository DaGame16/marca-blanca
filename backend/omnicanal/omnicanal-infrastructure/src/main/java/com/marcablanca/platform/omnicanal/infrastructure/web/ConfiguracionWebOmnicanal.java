package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.out.ModuloOmnicanalHabilitado;
import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cableado web propio de omnicanal:
 *  - FiltroTenantOmnicanalWebhook, registrado SOLO para el path del webhook
 *    (y el filtro NO lleva @Component) para que no sea un filtro global: la
 *    resolucion de tenant por secreto solo aplica a /api/v1/omnicanal/webhook/**;
 *    el resto de omnicanal resuelve el tenant por JWT (JwtAuthFilter).
 *  - InterceptorModuloOmnicanal, para TODAS las rutas de omnicanal (webhook
 *    incluido): corta con 403 si la empresa no tiene el modulo activo.
 */
@Configuration
class ConfiguracionWebOmnicanal implements WebMvcConfigurer {

    private final ModuloOmnicanalHabilitado moduloHabilitado;

    ConfiguracionWebOmnicanal(ModuloOmnicanalHabilitado moduloHabilitado) {
        this.moduloHabilitado = moduloHabilitado;
    }

    @Bean
    FilterRegistrationBean<FiltroTenantOmnicanalWebhook> filtroTenantOmnicanalWebhook(
            ResolverEmpresaPorWebhookSecreto resolverEmpresa) {
        var registro = new FilterRegistrationBean<>(new FiltroTenantOmnicanalWebhook(resolverEmpresa));
        registro.addUrlPatterns("/api/v1/omnicanal/webhook/*");
        registro.setOrder(Ordered.LOWEST_PRECEDENCE);
        registro.setName("filtroTenantOmnicanalWebhook");
        return registro;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterceptorModuloOmnicanal(moduloHabilitado))
                .addPathPatterns("/api/v1/omnicanal/**");
    }
}
