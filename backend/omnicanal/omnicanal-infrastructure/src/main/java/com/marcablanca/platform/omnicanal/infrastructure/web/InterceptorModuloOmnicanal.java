package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.out.ModuloOmnicanalHabilitado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Corta con 403 cualquier ruta de omnicanal (webhook incluido) si la empresa
 * activa no tiene el modulo "omnicanal" contratado y activo. Para cuando llega
 * aca el tenant ya esta resuelto: lo puso JwtAuthFilter (rutas con JWT) o
 * FiltroTenantOmnicanalWebhook (webhook), ambos antes que los interceptores MVC.
 */
class InterceptorModuloOmnicanal implements HandlerInterceptor {

    private final ModuloOmnicanalHabilitado moduloHabilitado;

    InterceptorModuloOmnicanal(ModuloOmnicanalHabilitado moduloHabilitado) {
        this.moduloHabilitado = moduloHabilitado;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws java.io.IOException {
        if (moduloHabilitado.paraEmpresaActual()) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"codigo\":403,\"mensaje\":\"La empresa no tiene el modulo Omnicanal activo.\"}");
        return false;
    }
}
