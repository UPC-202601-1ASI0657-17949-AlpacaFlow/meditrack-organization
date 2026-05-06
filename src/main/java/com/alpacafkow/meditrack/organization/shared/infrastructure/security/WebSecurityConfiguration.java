package com.alpacafkow.meditrack.organization.shared.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Fase 6 — Seguridad.
 * <p><b>Opción A (Sprint 1, pruebas de funcionamiento):</b> {@code anyRequest().permitAll()} para Postman,
 * Swagger y evidencias locales sin IAM. La iteración ADD “Seguridad” documentará el endurecimiento
 * (JWT offline, matriz por endpoint, etc.).</p>
 * <p><b>Opción B (posterior):</b> sustituir {@code permitAll()} por reglas por ruta + filtro Bearer JWT,
 * manteniendo este {@link UnauthorizedRequestHandlerEntryPoint} para respuestas 401 coherentes.</p>
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedRequestHandlerEntryPoint))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
