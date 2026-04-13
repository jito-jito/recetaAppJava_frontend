package com.duoc.seguridadcalidad.servicios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Servicio que centraliza todas las comunicaciones HTTP con el backend API.
 * Elimina duplicación de código en WebController y facilita los tests unitarios.
 */
@Service
public class BackendApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE_REF =
            new TypeReference<>() {};

    @Value("${backend.url:http://localhost:8080}")
    private String backendUrl;

    /**
     * Constructor principal para Spring (producción).
     * @Autowired indica a Spring que use este constructor para inyectar ObjectMapper.
     */
    @org.springframework.beans.factory.annotation.Autowired
    public BackendApiService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    /**
     * Constructor para tests — permite inyectar HttpClient mock
     */
    BackendApiService(HttpClient httpClient, ObjectMapper objectMapper, String backendUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.backendUrl = backendUrl;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    // ========================================================================
    // Métodos HTTP genéricos
    // ========================================================================

    /**
     * Envía un GET request. Si token es null, se envía sin autenticación.
     */
    public HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + path))
                .header(HEADER_ACCEPT, APPLICATION_JSON)
                .GET();
        if (token != null) {
            builder.header(HEADER_AUTHORIZATION, token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Envía un POST request con body JSON.
     */
    public HttpResponse<String> postJson(String path, String token, String jsonBody) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + path))
                .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (token != null) {
            builder.header(HEADER_AUTHORIZATION, token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Envía un POST request sin body.
     */
    public HttpResponse<String> postEmpty(String path, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + path))
                .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Envía un PUT request sin body.
     */
    public HttpResponse<String> put(String path, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + path))
                .header(HEADER_AUTHORIZATION, token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Envía un DELETE request.
     */
    public HttpResponse<String> delete(String path, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + path))
                .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, token)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ========================================================================
    // Métodos de utilidad para serialización
    // ========================================================================

    /**
     * Parsea un JSON string a una lista de mapas.
     */
    public List<Map<String, Object>> parseListResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, LIST_MAP_TYPE_REF);
    }

    /**
     * Parsea un JSON string a un mapa.
     */
    public Map<String, Object> parseMapResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Serializa un objeto a JSON string.
     */
    public String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
