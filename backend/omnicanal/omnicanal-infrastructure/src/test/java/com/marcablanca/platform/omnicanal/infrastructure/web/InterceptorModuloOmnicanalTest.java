package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.out.ModuloOmnicanalHabilitado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorModuloOmnicanalTest {

    @Mock
    ModuloOmnicanalHabilitado moduloHabilitado;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    private InterceptorModuloOmnicanal interceptor() {
        return new InterceptorModuloOmnicanal(moduloHabilitado);
    }

    @Test
    void modulo_activo_deja_pasar() throws Exception {
        when(moduloHabilitado.paraEmpresaActual()).thenReturn(true);

        assertTrue(interceptor().preHandle(request, response, new Object()));
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void modulo_inactivo_corta_con_403() throws Exception {
        when(moduloHabilitado.paraEmpresaActual()).thenReturn(false);
        StringWriter cuerpo = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpo));

        assertFalse(interceptor().preHandle(request, response, new Object()));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(cuerpo.toString().contains("\"codigo\":403"));
    }
}
