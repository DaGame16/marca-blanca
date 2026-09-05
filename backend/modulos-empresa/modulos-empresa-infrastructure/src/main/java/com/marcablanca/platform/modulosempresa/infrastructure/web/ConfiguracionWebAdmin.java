package com.marcablanca.platform.modulosempresa.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class ConfiguracionWebAdmin implements WebMvcConfigurer {

    private final ClaveAdminInterceptor claveAdminInterceptor;

    ConfiguracionWebAdmin(ClaveAdminInterceptor claveAdminInterceptor) {
        this.claveAdminInterceptor = claveAdminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(claveAdminInterceptor).addPathPatterns("/api/v1/admin/**");
    }
}
