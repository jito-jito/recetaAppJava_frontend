package com.duoc.seguridadcalidad.controladores;

import com.duoc.seguridadcalidad.servicios.BackendApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para WebController.
 * Usa @WebMvcTest para cargar solo la capa web y @MockBean para simular BackendApiService.
 */
@WebMvcTest(WebController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackendApiService backendApi;

    // ========================================================================
    // Helpers
    // ========================================================================

    private MockHttpSession createAuthSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("jwtToken", "Bearer test-token-123");
        session.setAttribute("username", "testuser");
        return session;
    }

    /**
     * Crea un stub de HttpResponse<String> usando clase anónima para evitar el problema
     * de UnfinishedStubbing que ocurre al usar mock(HttpResponse.class) de Mockito dentro
     * de when(...).thenReturn(mockResponse(...)).
     *
     * El problema: cuando Java evalúa los argumentos de thenReturn(), primero ejecuta mockResponse(),
     * que internamente llama a when(response.statusCode()).thenReturn(...). Esto registra una
     * stubbing activa en Mockito ANTES de que el thenReturn() externo se complete, corrompiendo
     * el estado y lanzando UnfinishedStubbingException.
     *
     * La solución es implementar HttpResponse<String> directamente sin usar mock().
     */
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        return new HttpResponse<String>() {
            @Override
            public int statusCode() { return statusCode; }

            @Override
            public String body() { return body; }

            @Override
            public HttpRequest request() { return null; }

            @Override
            public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }

            @Override
            public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (a, b) -> true); }

            @Override
            public Optional<SSLSession> sslSession() { return Optional.empty(); }

            @Override
            public URI uri() { return null; }

            @Override
            public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        };
    }

    // ========================================================================
    // Tests: GET / (inicio)
    // ========================================================================

    @Test
    void inicio_sinAutenticacion_muestraRecetas() throws Exception {
        when(backendApi.get(eq("/recipes"), isNull()))
                .thenReturn(mockResponse(200, "[]"));
        when(backendApi.parseListResponse("[]"))
                .thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("authenticated", false))
                .andExpect(model().attribute("recetas", List.of()));
    }

    @Test
    void inicio_conAutenticacion_muestraRecetasYFavoritos() throws Exception {
        MockHttpSession session = createAuthSession();

        // Datos completos para que Thymeleaf pueda renderizar el template sin NPE
        Map<String, Object> recetaMock = new java.util.HashMap<>();
        recetaMock.put("id", 1);
        recetaMock.put("idReceta", 1);
        recetaMock.put("titulo", "Receta Test");
        recetaMock.put("descripcion", "Descripcion");
        recetaMock.put("dificultad", "BAJA");
        recetaMock.put("tiempoPreparacion", 30);
        recetaMock.put("tiempoCoccion", 15);
        recetaMock.put("tipoCocina", "italiana");
        recetaMock.put("paisOrigen", "Italia");
        recetaMock.put("puntajePromedio", 4.5);
        recetaMock.put("cantidadComentarios", 0);
        recetaMock.put("mediaUrl", null);
        recetaMock.put("mediaType", null);
        recetaMock.put("publicada", true);
        recetaMock.put("ingredientes", List.of());
        recetaMock.put("pasos", List.of());
        recetaMock.put("instrucciones", List.of());
        recetaMock.put("comentarios", List.of());
        recetaMock.put("imagenes", List.of());
        recetaMock.put("videos", List.of());

        when(backendApi.get(eq("/recipes"), isNull()))
                .thenReturn(mockResponse(200, "[{\"id\":1}]"));
        when(backendApi.parseListResponse("[{\"id\":1}]"))
                .thenReturn(List.of(recetaMock));

        when(backendApi.get(eq("/api/usuarios/favoritos"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "[{\"idReceta\":1}]"));
        when(backendApi.parseListResponse("[{\"idReceta\":1}]"))
                .thenReturn(List.of(Map.of("idReceta", 1)));

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("username", "testuser"))
                .andExpect(model().attributeExists("recetas"))
                .andExpect(model().attributeExists("favoritosIds"));
    }

    @Test
    void inicio_backendCaido_muestraError() throws Exception {
        when(backendApi.get(eq("/recipes"), isNull()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("recetas", List.of()));
    }

    @Test
    void inicio_backendRetornaError_muestraListaVacia() throws Exception {
        when(backendApi.get(eq("/recipes"), isNull()))
                .thenReturn(mockResponse(500, "Internal Server Error"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("recetas", List.of()));
    }

    // ========================================================================
    // Tests: GET /login
    // ========================================================================

    @Test
    void login_sinSesion_muestraLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_conSesion_redireccionaADetalle() throws Exception {
        MockHttpSession session = createAuthSession();

        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/detalle"));
    }

    // ========================================================================
    // Tests: GET /detalle
    // ========================================================================

    @Test
    void detalle_sinAutenticacion_redireccionaALogin() throws Exception {
        mockMvc.perform(get("/detalle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void detalle_conAutenticacion_muestraFavoritosYMisRecetas() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.get(eq("/api/usuarios/favoritos"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "[]"));
        when(backendApi.get(eq("/recipes/mis-recetas"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "[]"));
        when(backendApi.parseListResponse("[]")).thenReturn(List.of());

        mockMvc.perform(get("/detalle").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attribute("username", "testuser"))
                .andExpect(model().attributeExists("recetas"))
                .andExpect(model().attributeExists("misRecetas"));
    }

    @Test
    void detalle_backendCaido_muestraError() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.get(eq("/api/usuarios/favoritos"), any()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(get("/detalle").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle"))
                .andExpect(model().attributeExists("error"));
    }

    // ========================================================================
    // Tests: GET /crear-receta
    // ========================================================================

    @Test
    void crearReceta_sinAutenticacion_redireccionaALogin() throws Exception {
        mockMvc.perform(get("/crear-receta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void crearReceta_conAutenticacion_muestraFormulario() throws Exception {
        MockHttpSession session = createAuthSession();

        mockMvc.perform(get("/crear-receta").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("crear-receta"))
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attributeExists("apiToken"));
    }

    // ========================================================================
    // Tests: GET /receta/{id}
    // ========================================================================

    @Test
    void verReceta_encontrada_muestraDetalle() throws Exception {
        Map<String, Object> recetaMock = new java.util.HashMap<>();
        recetaMock.put("id", 1);
        recetaMock.put("idReceta", 1);
        recetaMock.put("titulo", "Test");
        recetaMock.put("descripcion", "Descripcion");
        recetaMock.put("dificultad", "BAJA");
        recetaMock.put("tiempoPreparacion", 30);
        recetaMock.put("tiempoCoccion", 15);
        recetaMock.put("tipoCocina", "italiana");
        recetaMock.put("paisOrigen", "Italia");
        recetaMock.put("puntajePromedio", 4.5);
        recetaMock.put("cantidadComentarios", 0);
        recetaMock.put("mediaUrl", null);
        recetaMock.put("mediaType", null);
        recetaMock.put("publicada", true);
        recetaMock.put("ingredientes", List.of());
        recetaMock.put("pasos", List.of());
        recetaMock.put("instrucciones", List.of());
        recetaMock.put("comentarios", List.of());
        recetaMock.put("imagenes", List.of());
        recetaMock.put("videos", List.of());

        when(backendApi.get(eq("/recipes/1"), isNull()))
                .thenReturn(mockResponse(200, "{\"id\":1,\"titulo\":\"Test\"}"));
        when(backendApi.parseMapResponse(any()))
                .thenReturn(recetaMock);

        mockMvc.perform(get("/receta/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receta-detalle"))
                .andExpect(model().attributeExists("receta"));
    }

    @Test
    void verReceta_noEncontrada_muestraError() throws Exception {
        when(backendApi.get(eq("/recipes/999"), isNull()))
                .thenReturn(mockResponse(404, "Not Found"));

        mockMvc.perform(get("/receta/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("receta-detalle"))
                .andExpect(model().attribute("error", "No se encontró la receta"));
    }

    @Test
    void verReceta_conSesion_incluyeApiToken() throws Exception {
        MockHttpSession session = createAuthSession();

        Map<String, Object> recetaMock = new java.util.HashMap<>();
        recetaMock.put("id", 1);
        recetaMock.put("idReceta", 1);
        recetaMock.put("titulo", "Test");
        recetaMock.put("descripcion", "Descripcion");
        recetaMock.put("dificultad", "MEDIA");
        recetaMock.put("tiempoPreparacion", 20);
        recetaMock.put("tiempoCoccion", 10);
        recetaMock.put("tipoCocina", "italiana");
        recetaMock.put("paisOrigen", "Italia");
        recetaMock.put("puntajePromedio", 3.0);
        recetaMock.put("cantidadComentarios", 0);
        recetaMock.put("mediaUrl", null);
        recetaMock.put("mediaType", null);
        recetaMock.put("publicada", true);
        recetaMock.put("ingredientes", List.of());
        recetaMock.put("pasos", List.of());
        recetaMock.put("instrucciones", List.of());
        recetaMock.put("comentarios", List.of());
        recetaMock.put("imagenes", List.of());
        recetaMock.put("videos", List.of());

        when(backendApi.get(eq("/recipes/1"), isNull()))
                .thenReturn(mockResponse(200, "{\"id\":1}"));
        when(backendApi.parseMapResponse(any()))
                .thenReturn(recetaMock);

        mockMvc.perform(get("/receta/1").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("authenticated", true))
                .andExpect(model().attributeExists("apiToken"));
    }

    // ========================================================================
    // Tests: POST /api/login
    // ========================================================================

    @Test
    void loginPost_exitoso_retornaSuccessYRedirect() throws Exception {
        when(backendApi.toJson(any())).thenReturn("{\"username\":\"user\",\"password\":\"pass\"}");
        when(backendApi.postJson(eq("/login"), isNull(), any()))
                .thenReturn(mockResponse(200, "Bearer eyJhbGciOiJIUzI1NiJ9.test"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.redirectUrl").value("/detalle"));
    }

    @Test
    void loginPost_credencialesInvalidas_retornaError() throws Exception {
        when(backendApi.toJson(any())).thenReturn("{}");
        when(backendApi.postJson(eq("/login"), isNull(), any()))
                .thenReturn(mockResponse(401, "Unauthorized"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"bad\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void loginPost_tokenInvalido_retornaError() throws Exception {
        when(backendApi.toJson(any())).thenReturn("{}");
        when(backendApi.postJson(eq("/login"), isNull(), any()))
                .thenReturn(mockResponse(200, "InvalidToken"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token inválido recibido del backend"));
    }

    @Test
    void loginPost_backendCaido_retornaError() throws Exception {
        when(backendApi.toJson(any())).thenReturn("{}");
        when(backendApi.postJson(eq("/login"), isNull(), any()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Connection refused")));
    }

    // ========================================================================
    // Tests: POST /api/logout
    // ========================================================================

    @Test
    void logout_limpiaSessionYRetornaRedirect() throws Exception {
        MockHttpSession session = createAuthSession();

        mockMvc.perform(post("/api/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.redirectUrl").value("/login"));
    }

    // ========================================================================
    // Tests: POST /api/check-auth
    // ========================================================================

    @Test
    void checkAuth_conSesion_retornaAutenticado() throws Exception {
        MockHttpSession session = createAuthSession();

        mockMvc.perform(post("/api/check-auth").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void checkAuth_sinSesion_retornaNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/check-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    // ========================================================================
    // Tests: Favoritos
    // ========================================================================

    @Test
    void agregarFavorito_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/favoritos/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void agregarFavorito_exitoso_retornaSuccess() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.postEmpty(eq("/api/usuarios/favoritos/1"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "OK"));

        mockMvc.perform(post("/api/favoritos/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recetaId").value(1));
    }

    @Test
    void agregarFavorito_error_retornaFailed() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.postEmpty(any(), any()))
                .thenReturn(mockResponse(500, "Error"));

        mockMvc.perform(post("/api/favoritos/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void quitarFavorito_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(delete("/api/favoritos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quitarFavorito_exitoso_retornaSuccess() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.delete(eq("/api/usuarios/favoritos/1"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "OK"));

        mockMvc.perform(delete("/api/favoritos/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recetaId").value(1));
    }

    @Test
    void obtenerFavoritos_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/favoritos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerFavoritos_exitoso_retornaLista() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.get(eq("/api/usuarios/favoritos"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "[{\"id\":1}]"));
        when(backendApi.parseListResponse("[{\"id\":1}]"))
                .thenReturn(List.of(Map.of("id", 1)));

        mockMvc.perform(get("/api/favoritos").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerFavoritos_backendCaido_retorna500() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.get(any(), any()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(get("/api/favoritos").session(session))
                .andExpect(status().isInternalServerError());
    }

    // ========================================================================
    // Tests: PUT /api/recipes/{id}/estado
    // ========================================================================

    @Test
    void cambiarEstado_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(put("/api/recipes/1/estado").param("publicada", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cambiarEstado_exitoso_retornaRespuestaBackend() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.put(eq("/recipes/1/estado?publicada=true"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "{\"publicada\":true}"));

        mockMvc.perform(put("/api/recipes/1/estado")
                        .param("publicada", "true")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_backendCaido_retorna500() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.put(any(), any()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(put("/api/recipes/1/estado")
                        .param("publicada", "true")
                        .session(session))
                .andExpect(status().isInternalServerError());
    }

    // ========================================================================
    // Tests: Comentarios proxy
    // ========================================================================

    @Test
    void getComentarios_retornaRespuestaBackend() throws Exception {
        when(backendApi.get(eq("/recipes/1/comentarios"), isNull()))
                .thenReturn(mockResponse(200, "[{\"texto\":\"Buenísimo\"}]"));

        mockMvc.perform(get("/api/recipes/1/comentarios"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"texto\":\"Buenísimo\"}]"));
    }

    @Test
    void getComentarios_backendCaido_retorna500() throws Exception {
        when(backendApi.get(any(), isNull()))
                .thenThrow(new java.io.IOException("Connection refused"));

        mockMvc.perform(get("/api/recipes/1/comentarios"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postComentario_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/recipes/1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Hola\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComentario_exitoso_retornaRespuestaBackend() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.toJson(any())).thenReturn("{\"texto\":\"Hola\"}");
        when(backendApi.postJson(eq("/recipes/1/comentarios"), eq("Bearer test-token-123"), any()))
                .thenReturn(mockResponse(201, "{\"id\":1,\"texto\":\"Hola\"}"));

        mockMvc.perform(post("/api/recipes/1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Hola\"}")
                        .session(session))
                .andExpect(status().isCreated());
    }

    // ========================================================================
    // Tests: Valoraciones proxy
    // ========================================================================

    @Test
    void getValoraciones_retornaRespuestaBackend() throws Exception {
        when(backendApi.get(eq("/recipes/1/valoraciones"), isNull()))
                .thenReturn(mockResponse(200, "{\"promedio\":4.5}"));

        mockMvc.perform(get("/api/recipes/1/valoraciones"))
                .andExpect(status().isOk());
    }

    @Test
    void getValoraciones_conAuth_enviaToken() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.get(eq("/recipes/1/valoraciones"), eq("Bearer test-token-123")))
                .thenReturn(mockResponse(200, "{}"));

        mockMvc.perform(get("/api/recipes/1/valoraciones").session(session))
                .andExpect(status().isOk());

        verify(backendApi).get("/recipes/1/valoraciones", "Bearer test-token-123");
    }

    @Test
    void postValoracion_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/recipes/1/valoraciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntaje\":5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postValoracion_exitoso_retornaRespuestaBackend() throws Exception {
        MockHttpSession session = createAuthSession();

        when(backendApi.toJson(any())).thenReturn("{\"puntaje\":5}");
        when(backendApi.postJson(eq("/recipes/1/valoraciones"), eq("Bearer test-token-123"), any()))
                .thenReturn(mockResponse(201, "{\"id\":1}"));

        mockMvc.perform(post("/api/recipes/1/valoraciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"puntaje\":5}")
                        .session(session))
                .andExpect(status().isCreated());
    }
}
