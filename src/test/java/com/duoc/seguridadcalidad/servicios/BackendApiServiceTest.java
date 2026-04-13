package com.duoc.seguridadcalidad.servicios;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BackendApiService.
 * Usa un HttpClient mock para simular respuestas del backend sin conexión real.
 */
class BackendApiServiceTest {

    private HttpClient mockClient;
    private BackendApiService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crea un mock de HttpResponse<String> con estado y body dados.
     * El cast sin warning se justifica porque es un mock de test.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private HttpResponse<String> mockResponse(int status, String body) {
        HttpResponse mock = mock(HttpResponse.class);
        when(mock.statusCode()).thenReturn(status);
        when(mock.body()).thenReturn(body);
        return (HttpResponse<String>) mock;
    }

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        service = new BackendApiService(mockClient, objectMapper, "http://localhost:8080");
    }

    // ========================================================================
    // Tests: GET
    // ========================================================================

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void get_sinToken_enviaRequestSinAuth() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "[]");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.get("/recipes", null);

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
        verify(mockClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void get_conToken_enviaRequestConAuth() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "[{\"id\":1}]");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.get("/api/usuarios/favoritos", "Bearer token123");

        assertEquals(200, response.statusCode());
        verify(mockClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void get_backendCaido_lanzaExcepcion() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("Connection refused"));

        assertThrows(java.io.IOException.class, () -> service.get("/recipes", null));
    }

    // ========================================================================
    // Tests: POST JSON
    // ========================================================================

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void postJson_exitoso_enviaBodyYHeaders() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "Bearer eyJ...");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.postJson(
                "/login", null, "{\"username\":\"user\",\"password\":\"pass\"}");

        assertEquals(200, response.statusCode());
        verify(mockClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void postJson_conToken_incluyeAutorizacion() throws Exception {
        HttpResponse<String> resp = mockResponse(201, "{\"id\":1}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.postJson(
                "/recipes/1/comentarios", "Bearer tok", "{\"texto\":\"Bien\"}");

        assertEquals(201, response.statusCode());
    }

    // ========================================================================
    // Tests: POST Empty
    // ========================================================================

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void postEmpty_exitoso() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "OK");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.postEmpty(
                "/api/usuarios/favoritos/1", "Bearer token");

        assertEquals(200, response.statusCode());
    }

    // ========================================================================
    // Tests: PUT
    // ========================================================================

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void put_exitoso() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "{\"publicada\":true}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.put(
                "/recipes/1/estado?publicada=true", "Bearer token");

        assertEquals(200, response.statusCode());
    }

    // ========================================================================
    // Tests: DELETE
    // ========================================================================

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete_exitoso() throws Exception {
        HttpResponse<String> resp = mockResponse(200, "OK");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp);

        HttpResponse<String> response = service.delete(
                "/api/usuarios/favoritos/1", "Bearer token");

        assertEquals(200, response.statusCode());
    }

    // ========================================================================
    // Tests: Utilidades de serialización
    // ========================================================================

    @Test
    void parseListResponse_parseaCorrectamente() throws Exception {
        String json = "[{\"id\":1,\"nombre\":\"Pasta\"},{\"id\":2,\"nombre\":\"Sopa\"}]";

        List<Map<String, Object>> result = service.parseListResponse(json);

        assertEquals(2, result.size());
        assertEquals("Pasta", result.get(0).get("nombre"));
        assertEquals("Sopa", result.get(1).get("nombre"));
    }

    @Test
    void parseMapResponse_parseaCorrectamente() throws Exception {
        String json = "{\"id\":1,\"nombre\":\"Pasta Carbonara\"}";

        Map<String, Object> result = service.parseMapResponse(json);

        assertEquals(1, result.get("id"));
        assertEquals("Pasta Carbonara", result.get("nombre"));
    }

    @Test
    void toJson_serializaCorrectamente() throws Exception {
        // Usamos LinkedHashMap para orden determinístico
        Map<String, String> data = new java.util.LinkedHashMap<>();
        data.put("username", "user");
        data.put("password", "pass");

        String result = service.toJson(data);

        assertTrue(result.contains("\"username\""));
        assertTrue(result.contains("\"password\""));
    }

    @Test
    void parseListResponse_jsonInvalido_lanzaExcepcion() {
        assertThrows(Exception.class, () -> service.parseListResponse("invalid json"));
    }

    @Test
    void getBackendUrl_retornaUrlConfigurada() {
        assertEquals("http://localhost:8080", service.getBackendUrl());
    }
}
