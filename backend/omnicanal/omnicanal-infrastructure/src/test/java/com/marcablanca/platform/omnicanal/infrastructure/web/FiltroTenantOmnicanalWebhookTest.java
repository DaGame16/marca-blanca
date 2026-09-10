package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiltroTenantOmnicanalWebhookTest {

    @Mock
    ResolverEmpresaPorWebhookSecreto resolverEmpresa;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;

    @AfterEach
    void limpiarContexto() {
        ContextoEmpresaActual.limpiar();
    }

    private FiltroTenantOmnicanalWebhook filtro() {
        return new FiltroTenantOmnicanalWebhook(resolverEmpresa);
    }

    @Test
    void secreto_valido_fija_el_tenant_durante_la_cadena_y_lo_limpia_despues() throws Exception {
        when(request.getHeader(FiltroTenantOmnicanalWebhook.HEADER_SECRETO)).thenReturn("s3cr3t");
        when(resolverEmpresa.resolverIdentificadorEmpresa("s3cr3t")).thenReturn(Optional.of("acme"));

        AtomicReference<String> tenantEnLaCadena = new AtomicReference<>();
        doAnswer(inv -> {
            tenantEnLaCadena.set(ContextoEmpresaActual.obtener().orElse(null));
            return null;
        }).when(chain).doFilter(request, response);

        filtro().doFilterInternal(request, response, chain);

        assertEquals("acme", tenantEnLaCadena.get());
        assertTrue(ContextoEmpresaActual.obtener().isEmpty(), "el ThreadLocal debe quedar limpio tras el filtro");
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void secreto_invalido_responde_401_y_no_sigue_la_cadena() throws Exception {
        when(request.getHeader(FiltroTenantOmnicanalWebhook.HEADER_SECRETO)).thenReturn("nope");
        when(resolverEmpresa.resolverIdentificadorEmpresa("nope")).thenReturn(Optional.empty());
        StringWriter cuerpo = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpo));

        filtro().doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
        assertTrue(cuerpo.toString().contains("\"codigo\":401"));
        assertTrue(ContextoEmpresaActual.obtener().isEmpty());
    }

    @Test
    void limpia_el_contexto_aunque_la_cadena_lance() throws Exception {
        when(request.getHeader(FiltroTenantOmnicanalWebhook.HEADER_SECRETO)).thenReturn("s3cr3t");
        when(resolverEmpresa.resolverIdentificadorEmpresa("s3cr3t")).thenReturn(Optional.of("acme"));
        doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

        assertThrows(ServletException.class, () -> filtro().doFilterInternal(request, response, chain));
        assertTrue(ContextoEmpresaActual.obtener().isEmpty());
    }
}
