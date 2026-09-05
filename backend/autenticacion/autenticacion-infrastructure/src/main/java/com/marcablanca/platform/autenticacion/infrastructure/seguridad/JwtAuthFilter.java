package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import com.marcablanca.platform.autenticacion.application.port.out.UsuarioAutenticado;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeToken;
import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Corre en cada request. Si hay un Bearer token valido, marca al usuario
 * como autenticado Y establece ContextoEmpresaActual con la empresa del
 * token -- asi cualquier endpoint protegido por JWT (no solo login/refresh)
 * puede consultar la base de la empresa correcta, y self-service endpoints
 * (ej. marca blanca) saben de que empresa es el usuario sin confiar en
 * lo que mande el cliente en la URL.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String ATRIBUTO_EMPRESA = "identificadorEmpresa";

    private final VerificadorDeToken verificadorDeToken;

    public JwtAuthFilter(VerificadorDeToken verificadorDeToken) {
        this.verificadorDeToken = verificadorDeToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            String token = encabezado.substring(PREFIJO_BEARER.length());
            Optional<UsuarioAutenticado> usuarioAutenticado = verificadorDeToken.verificar(token);

            if (usuarioAutenticado.isPresent()) {
                UsuarioAutenticado datos = usuarioAutenticado.get();
                var autenticacion = new UsernamePasswordAuthenticationToken(datos.usuarioId(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
                request.setAttribute(ATRIBUTO_EMPRESA, datos.identificadorEmpresa());

                ContextoEmpresaActual.establecer(datos.identificadorEmpresa());
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    ContextoEmpresaActual.limpiar();
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
