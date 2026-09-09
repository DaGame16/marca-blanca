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

    /**
     * La consola de operacion (/api/v1/consola/**) tiene su propia cadena de
     * seguridad y su propio filtro. Este filtro de tenant esta registrado de
     * forma global, asi que sin este corte tambien correria ahi: como el token
     * de operador comparte el secreto de firma, lo validaria como si fuera de
     * tenant (con empresa nula) y, si trae pwd_temp, bloquearia con 403 el
     * propio cambio de contrasena del operador.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/consola/");
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

                // Contrasena temporal: hasta cambiarla, el token solo sirve para /api/v1/auth/**
                // (cambiar-contrasena, logout) y para las rutas publicas (permitAll en
                // SecurityConfig) que de todas formas no requieren este token para nada --
                // rechazarlas aca de mas era un bug real: el login de OTRA empresa hace un
                // GET publico a /api/v1/empresas/{id}/marca para pintar el logo, y si el
                // navegador todavia tenia guardado un token viejo con contrasena temporal
                // pendiente (de un intento de login anterior sin completar), ese GET publico
                // se rechazaba con 403 -- el logo/colores nunca se pintaban y parecia que la
                // marca "no se guardo", cuando en realidad si estaba en la base de datos.
                if (datos.debeCambiarContrasena() && esRutaQueRequiereContrasenaDefinitiva(request.getRequestURI())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"codigo\":403,\"mensaje\":\"Debes cambiar tu contrasena temporal antes de continuar.\"}");
                    return;
                }

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

    /**
     * Espejo del permitAll de SecurityConfig: si la ruta ya es publica (no
     * necesita JWT para nada), no tiene sentido bloquearla solo porque el
     * token que vino de arrastre tenga la contrasena temporal pendiente.
     * Cuando SecurityConfig gane una ruta publica nueva, agregarla aca
     * tambien.
     */
    private static boolean esRutaQueRequiereContrasenaDefinitiva(String uri) {
        return !uri.startsWith("/api/v1/auth/")
                && !uri.startsWith("/api/v1/registro/")
                && !uri.startsWith("/api/v1/admin/")
                && !uri.startsWith("/api/v1/omnicanal/webhook/")
                && !(uri.startsWith("/api/v1/empresas/") && uri.endsWith("/marca"));
    }
}
