package com.duoc.seguridadcalidad.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import java.util.EnumSet;

/**
 * Configuración de gestión de sesiones para mitigar vulnerabilidades relacionadas con sesiones
 */
@Configuration
public class SessionConfig {

    /**
     * **MITIGACIÓN: Session ID in URL Rewrite**
     * Configuración para prevenir que el Session ID aparezca en las URLs
     * Solo permite tracking por cookies, eliminando el riesgo de session ID en URL rewrite
     */
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return (ServletContext servletContext) -> {
            // **IMPORTANTE: Solo permitir tracking por cookies, NO por URL**
            servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));

            // **MITIGACIÓN: Cookie Security Configuration**
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setHttpOnly(true); // Prevenir acceso via JavaScript
            sessionCookieConfig.setSecure(false);   // Para desarrollo local (cambiar a true en producción)
            sessionCookieConfig.setMaxAge(1800);    // 30 minutos
            sessionCookieConfig.setName("JSESSIONID");
        };
    }
}