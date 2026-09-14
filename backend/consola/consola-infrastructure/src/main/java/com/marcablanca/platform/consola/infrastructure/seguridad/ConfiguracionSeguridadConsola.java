package com.marcablanca.platform.consola.infrastructure.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cadena de seguridad propia de la consola, con prioridad sobre la de tenant
 * ({@code @Order(1)} + {@code securityMatcher("/api/v1/consola/**")}). Para esas
 * rutas manda esta; el resto de la app sigue con la cadena de {@code autenticacion}.
 *
 * No repite {@code @EnableWebSecurity} (ya lo activa el modulo autenticacion).
 * El CORS se define aca y NO se comparte con la cadena de tenant: la consola vive
 * en su propio origen (panel.guajiranet.com en prod; el dev-server de Angular en local).
 */
@Configuration
public class ConfiguracionSeguridadConsola {

    private final List<String> origenesPermitidos;

    public ConfiguracionSeguridadConsola(
            @Value("${app.cors.origenes-permitidos:http://localhost:4200,http://*.localhost:4200}")
            List<String> origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain cadenaSeguridadConsola(HttpSecurity http, VerificadorJwtDeOperador verificador) {
        try {
            http
                    .securityMatcher("/api/v1/consola/**")
                    // CSRF es una defensa contra requests de formularios/cookies de otro origen
                    // -- no aplica aca: la sesion es un JWT en el header Authorization
                    // (STATELESS, sin cookies), que un sitio malicioso no puede leer ni forjar.
                    .csrf(csrf -> csrf.disable()) //NOSONAR ver comentario arriba
                    .cors(cors -> cors.configurationSource(corsConsola()))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/consola/auth/login").permitAll()
                            .anyRequest().authenticated())
                    .addFilterBefore(new ConsolaAuthFilter(verificador), UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Error al construir la cadena de filtros de seguridad de consola", e);
        }
    }

    private CorsConfigurationSource corsConsola() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(origenesPermitidos);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/v1/consola/**", configuracion);
        return fuente;
    }
}
