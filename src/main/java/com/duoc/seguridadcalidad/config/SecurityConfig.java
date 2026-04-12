package com.duoc.seguridadcalidad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para mitigar vulnerabilidades web
 * Incluye headers de seguridad, gestión de sesiones y políticas de cookies
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuración principal de seguridad
     * Aplica headers de seguridad y configuraciones para mitigar las vulnerabilidades detectadas
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configurar autorización - permitir acceso a todas las URLs del frontend
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            
            // Deshabilitar CSRF para APIs REST (el frontend maneja su propia autenticación con JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar headers de seguridad usando API moderna
            .headers(headers -> headers
                // **MITIGACIÓN: X-Frame-Options Header Missing / Anti-clickjacking**
                .frameOptions(frameOptions -> frameOptions.deny())
                
                // **MITIGACIÓN: X-Content-Type-Options Header Missing**
                .contentTypeOptions(contentType -> {})
                
                // **MITIGACIÓN: Content Security Policy (CSP) Header Not Set**
                // Política CSP estricta optimizada para desarrollo local
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: http://localhost:8080; " +
                        "connect-src 'self' http://localhost:8080 https://localhost:8080; " +
                        "frame-src 'none'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'; " +
                        "base-uri 'self'; " +
                        "object-src 'none'; " +
                        "media-src 'self' http://localhost:8080; " +
                        "manifest-src 'self'; " +
                        "worker-src 'none'"
                        // Removido 'upgrade-insecure-requests' para desarrollo local
                    )
                )
                
                // **MITIGACIÓN: HSTS Header**
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                
                // Headers personalizados para mitigar vulnerabilidades adicionales
                .addHeaderWriter((request, response) -> {
                    // **MITIGACIÓN: X-XSS-Protection Header Missing**
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    
                    // **MITIGACIÓN: Referrer Policy Header Missing**
                    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                    
                    // **MITIGACIÓN: Permissions Policy (Feature Policy)**
                    response.setHeader("Permissions-Policy", 
                        "camera=(), microphone=(), geolocation=(), " +
                        "accelerometer=(), gyroscope=(), magnetometer=(), " +
                        "payment=(), usb=()");
                })
            )
            
            // **MITIGACIÓN: Session ID in URL Rewrite / Session Management**
            .sessionManagement(session -> session
                // Política de creación de sesiones
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                
                // Limitar sesiones concurrentes usando API moderna
                .sessionConcurrency(concurrency -> concurrency
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                )
                
                // Configuraciones adicionales de sesión
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login")
            );

        return http.build();
    }
}