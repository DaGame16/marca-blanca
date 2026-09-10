package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Corre solo en /api/v1/omnicanal/webhook/** (lo acota ConfiguracionWebOmnicanal,
 * no es un filtro global). LIWA no manda JWT: el header x-liwa-webhook-secret ES
 * el dato de tenant. Este filtro lo resuelve contra
 * plataforma.tbl_empresas_omnicanal y deja la empresa en ContextoEmpresaActual
 * para que el enrutador multi-tenant escriba en la base correcta; si el secreto
 * no corresponde a ninguna empresa, corta con 401 y no llega al controlador.
 *
 * Escribe el 401 a mano (mismo enfoque que JwtAuthFilter): un filtro no pasa por
 * el @RestControllerAdvice del modulo.
 *
 * IMPORTANTE: limpia el ThreadLocal en un finally -- el contenedor reutiliza
 * hilos entre peticiones.
 */
public class FiltroTenantOmnicanalWebhook extends OncePerRequestFilter {

    static final String HEADER_SECRETO = "x-liwa-webhook-secret";

    private final ResolverEmpresaPorWebhookSecreto resolverEmpresa;

    public FiltroTenantOmnicanalWebhook(ResolverEmpresaPorWebhookSecreto resolverEmpresa) {
        this.resolverEmpresa = resolverEmpresa;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String secreto = request.getHeader(HEADER_SECRETO);
        Optional<String> identificadorEmpresa = resolverEmpresa.resolverIdentificadorEmpresa(secreto);

        if (identificadorEmpresa.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"codigo\":401,\"mensaje\":\"Secreto de webhook invalido o empresa no configurada para Omnicanal.\"}");
            return;
        }

        ContextoEmpresaActual.establecer(identificadorEmpresa.get());
        try {
            filterChain.doFilter(request, response);
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }
}
