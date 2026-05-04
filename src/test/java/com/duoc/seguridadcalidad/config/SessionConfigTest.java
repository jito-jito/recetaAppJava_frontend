package com.duoc.seguridadcalidad.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import java.util.EnumSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionConfigTest {

    @Test
    void servletContextInitializer_configuraSesionDeFormaSegura() throws Exception {
        SessionConfig sessionConfig = new SessionConfig();
        ServletContextInitializer initializer = sessionConfig.servletContextInitializer();

        ServletContext servletContext = mock(ServletContext.class);
        SessionCookieConfig sessionCookieConfig = mock(SessionCookieConfig.class);

        org.mockito.Mockito.when(servletContext.getSessionCookieConfig())
                .thenReturn(sessionCookieConfig);

        initializer.onStartup(servletContext);

        verify(servletContext).setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        verify(servletContext).getSessionCookieConfig();
        verify(sessionCookieConfig).setHttpOnly(true);
        verify(sessionCookieConfig).setSecure(false);
        verify(sessionCookieConfig).setMaxAge(1800);
        verify(sessionCookieConfig).setName("JSESSIONID");
    }
}