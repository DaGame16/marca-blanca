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

    /**
     * Origenes permitidos para CORS, separados por coma. En dev por defecto
     * solo http://localhost:4200 (ng serve) -- en cualquier otro ambiente
     * se sobreescribe con la variable de entorno CORS_ALLOWED_ORIGINS,
     * nunca se deja "*" porque las peticiones autenticadas van con
     * Authorization/X-Admin-Key (allowCredentials no es compatible con
     * origen comodin de todos modos).
     */
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) {
        try {
            http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/api/v1/registro/**").permitAll()
                            // /api/v1/admin/** NO usa JWT -- se protege con clave compartida
                            // (ClaveAdminInterceptor, modulo empresas). Ver ADR del modulo de
                            // modulos-por-empresa para el porque de esta decision interina.
                            .requestMatchers("/api/v1/admin/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Error al construir la cadena de filtros de seguridad", e);
        }
    }

    /**
     * Sin esto, cualquier llamada del frontend (Angular en un origen
     * distinto al del backend) queda bloqueada por el navegador antes de
     * llegar a los controllers -- Spring Security no habilita CORS por
     * defecto aunque el filtro de la request nunca la rechace del lado
     * del servidor.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Admin-Key"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
