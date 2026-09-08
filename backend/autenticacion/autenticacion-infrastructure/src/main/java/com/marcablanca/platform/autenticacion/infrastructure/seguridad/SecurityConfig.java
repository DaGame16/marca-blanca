package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                           @Value("${app.cors.origenes-permitidos:http://localhost:4200}") List<String> origenesPermitidos) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) {
        try {
            http
                    .csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/api/v1/registro/**").permitAll()
                            // /api/v1/admin/** NO usa JWT -- se protege con clave compartida
                            // (ClaveAdminInterceptor, modulo empresas). Ver ADR del modulo de
                            // modulos-por-empresa para el porque de esta decision interina.
                            .requestMatchers("/api/v1/admin/**").permitAll()
                            // El webhook de LIWA no tiene sesion humana -- el secreto en el
                            // header ES el mecanismo de autenticacion/identificacion de tenant
                            // (ver ResolverEmpresaPorWebhookSecreto, modulo omnicanal).
                            .requestMatchers("/api/v1/omnicanal/webhook/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Error al construir la cadena de filtros de seguridad", e);
        }
    }

    /**
     * Sin esto, un navegador bloquea la lectura de la respuesta cuando el
     * frontend (localhost:4200 en desarrollo) y el backend (localhost:8080)
     * son origenes distintos -- aunque el backend responda 200 igual, el
     * navegador nunca deja que el JS lea el cuerpo. No aparece nunca
     * probando con curl/Postman, porque CORS es una regla exclusiva de
     * navegadores, no del servidor en si.
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(origenesPermitidos);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Admin-Key"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
