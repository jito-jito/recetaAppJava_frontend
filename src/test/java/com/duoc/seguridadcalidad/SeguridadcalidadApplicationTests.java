package com.duoc.seguridadcalidad;

import com.duoc.seguridadcalidad.servicios.BackendApiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test de carga del contexto de Spring Boot.
 * BackendApiService se mockea para evitar conexiones reales al backend durante el test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SeguridadcalidadApplicationTests {

    @MockBean
    private BackendApiService backendApiService;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring carga correctamente
    }

}
