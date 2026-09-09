package com.marcablanca.platform.consola.infrastructure.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Se monta solo en la cadena de seguridad de {@code /api/v1/consola/**}. Si hay
 * un Bearer token de operador valido, deja al operador autenticado (principal =
 * su UUID, authority = ROLE_&lt;rol&gt;). Mientras la contrasena sea temporal, el
 * token solo sirve para {@code /api/v1/consola/auth/**}; cualquier otra ruta
 * responde 403.
 */
public class ConsolaAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final VerificadorJwtDeOperador verificador;

    public ConsolaAuthFilter(VerificadorJwtDeOperador verificador) {
        this.verificador = verificador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            Optional<OperadorAutenticado> operador =
                    verificador.verificar(encabezado.substring(PREFIJO_BEARER.length()));

            if (operador.isPresent()) {
                OperadorAutenticado datos = operador.get();
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        datos.operadorId(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + datos.rol())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);

                if (datos.debeCambiarContrasena()
                        && !request.getRequestURI().startsWith("/api/v1/consola/auth/")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"codigo\":403,\"mensaje\":\"Debes cambiar tu contrasena temporal antes de continuar.\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
