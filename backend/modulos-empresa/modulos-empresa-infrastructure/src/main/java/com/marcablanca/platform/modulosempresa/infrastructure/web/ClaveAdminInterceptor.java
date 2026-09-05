package com.marcablanca.platform.modulosempresa.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Proteccion INTERINA por clave compartida -- no hay todavia un sistema de
 * identidad de "administrador de plataforma" (documentado como decision
 * consciente, ver la ADR de este modulo). Se usa un HandlerInterceptor y NO
 * un Filter a proposito: los interceptores corren dentro del procesamiento
 * de Spring MVC, siempre DESPUES de que la cadena de Spring Security ya dejo
 * pasar el request -- evita el mismo tipo de problema de orden de arranque
 * que costo tanto resolver con el enrutador multi-tenant.
 */
@Component
class ClaveAdminInterceptor implements HandlerInterceptor {

    private static final String ENCABEZADO = "X-Admin-Key";

    private final String claveEsperada;

    ClaveAdminInterceptor(@Value("${app.admin.clave}") String claveEsperada) {
        this.claveEsperada = claveEsperada;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clave = request.getHeader(ENCABEZADO);
        if (clave == null || !clave.equals(claveEsperada)) {
            throw new ClaveAdminInvalidaException();
        }
        return true;
    }
}
