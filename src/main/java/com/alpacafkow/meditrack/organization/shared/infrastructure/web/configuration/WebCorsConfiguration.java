package com.alpacafkow.meditrack.organization.shared.infrastructure.web.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration.
 * <p>
 * This class configures Cross-Origin Resource Sharing (CORS) to allow requests from the frontend.
 * Currently allows requests from localhost:4200 (Angular development server).
 * </p>
 */
@Configuration
public class WebCorsConfiguration {
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Creates a CORS filter that allows requests from the frontend.
     * <p>
     * This configuration allows:
     * - Origins: Development (localhost:4200) and production domains
     * - Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * - Headers: All headers
     * - Credentials: Allowed
     * </p>
     *
     * @return The {@link CorsFilter} instance with CORS configuration
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        config.setAllowedOriginPatterns(origins);
        
        // Allow all HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow all headers
        config.setAllowedHeaders(List.of("*"));
        
        // Expose headers that frontend needs to read
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

