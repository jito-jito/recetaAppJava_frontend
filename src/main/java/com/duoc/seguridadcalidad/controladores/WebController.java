package com.duoc.seguridadcalidad.controladores;

import com.duoc.seguridadcalidad.servicios.BackendApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpSession;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Controlador web para manejar las páginas del frontend y conexión con backend
 */
@Controller
public class WebController {

    // --- Constantes de sesión ---
    private static final String SESSION_JWT = "jwtToken";
    private static final String SESSION_USER = "username";

    // --- Constantes de modelo (Thymeleaf) ---
    private static final String MODEL_AUTHENTICATED = "authenticated";
    private static final String MODEL_USERNAME = "username";
    private static final String MODEL_RECETAS = "recetas";
    private static final String MODEL_MIS_RECETAS = "misRecetas";
    private static final String MODEL_FAVORITOS_IDS = "favoritosIds";
    private static final String MODEL_ERROR = "error";

    // --- Constantes de respuesta JSON ---
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";

    // --- Constantes de mensajes ---
    private static final String MSG_NO_AUTORIZADO = "No autorizado";
    private static final String MSG_ERROR_CONEXION = "Error de conexión: ";

    // --- Servicio de backend ---
    private final BackendApiService backendApi;

    public WebController(BackendApiService backendApi) {
        this.backendApi = backendApi;
    }

    // ========================================================================
    // Métodos helper privados — eliminan duplicación de código
    // ========================================================================

    /** Obtiene el token JWT de la sesión, o null si no existe. */
    private String getToken(HttpSession session) {
        return (String) session.getAttribute(SESSION_JWT);
    }

    /** Obtiene el username de la sesión, o null si no existe. */
    private String getUsername(HttpSession session) {
        return (String) session.getAttribute(SESSION_USER);
    }

    /** Agrega información de autenticación al modelo Thymeleaf. */
    private void addAuthToModel(Model model, HttpSession session) {
        String token = getToken(session);
        String username = getUsername(session);
        if (token != null && username != null) {
            model.addAttribute(MODEL_AUTHENTICATED, true);
            model.addAttribute(MODEL_USERNAME, username);
        } else {
            model.addAttribute(MODEL_AUTHENTICATED, false);
        }
    }

    /** Helper: Proxy GET genérico al backend — retorna la respuesta tal cual. */
    private ResponseEntity<String> proxyGet(String path, String token) {
        try {
            HttpResponse<String> response = backendApi.get(path, token);
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        }
    }

    /** Helper: Proxy POST autenticado con body JSON al backend. */
    private ResponseEntity<String> proxyPostAuth(String path, Map<String, Object> body, HttpSession session) {
        String token = getToken(session);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MSG_NO_AUTORIZADO);
        }
        try {
            HttpResponse<String> response = backendApi.postJson(path, token, backendApi.toJson(body));
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        }
    }

    /** Helper: Toggle favorito (agregar/quitar) — lógica compartida. */
    private ResponseEntity<Map<String, Object>> toggleFavorito(
            Long recetaId, HttpSession session, boolean agregar) {
        Map<String, Object> response = new HashMap<>();
        String token = getToken(session);

        if (token == null) {
            response.put(KEY_SUCCESS, false);
            response.put(KEY_MESSAGE, "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String path = "/api/usuarios/favoritos/" + recetaId;
            HttpResponse<String> backendResponse = agregar
                    ? backendApi.postEmpty(path, token)
                    : backendApi.delete(path, token);

            if (backendResponse.statusCode() == 200) {
                response.put(KEY_SUCCESS, true);
                response.put(KEY_MESSAGE, agregar
                        ? "Receta agregada a favoritos exitosamente"
                        : "Receta quitada de favoritos exitosamente");
                response.put("recetaId", recetaId);
            } else {
                response.put(KEY_SUCCESS, false);
                response.put(KEY_MESSAGE, agregar
                        ? "Error al agregar a favoritos"
                        : "Error al quitar de favoritos");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.put(KEY_SUCCESS, false);
            response.put(KEY_MESSAGE, MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            response.put(KEY_SUCCESS, false);
            response.put(KEY_MESSAGE, MSG_ERROR_CONEXION + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /** Obtiene los IDs de recetas favoritas del usuario autenticado. */
    private Set<Object> obtenerFavoritosIds(String jwtToken) {
        try {
            HttpResponse<String> favoritosResponse = backendApi.get("/api/usuarios/favoritos", jwtToken);
            if (favoritosResponse.statusCode() == 200) {
                List<Map<String, Object>> favoritos = backendApi.parseListResponse(favoritosResponse.body());
                Set<Object> favoritosIds = new HashSet<>();
                for (Map<String, Object> favorito : favoritos) {
                    favoritosIds.add(favorito.get("idReceta"));
                }
                return favoritosIds;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Si hay error obteniendo favoritos, continuar sin ellos
        }
        return Collections.emptySet();
    }

    // ========================================================================
    // Endpoints de vistas (Thymeleaf)
    // ========================================================================

    /**
     * Página principal
     */
    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {
        addAuthToModel(model, session);
        String jwtToken = getToken(session);

        try {
            HttpResponse<String> response = backendApi.get("/recipes", null);

            if (response.statusCode() == 200) {
                model.addAttribute(MODEL_RECETAS, backendApi.parseListResponse(response.body()));
                model.addAttribute(MODEL_FAVORITOS_IDS,
                        jwtToken != null ? obtenerFavoritosIds(jwtToken) : Collections.emptySet());
            } else {
                model.addAttribute(MODEL_RECETAS, Collections.emptyList());
                model.addAttribute(MODEL_FAVORITOS_IDS, Collections.emptySet());
                model.addAttribute(MODEL_ERROR, "No se pudieron cargar las recetas del backend");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            model.addAttribute(MODEL_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_FAVORITOS_IDS, Collections.emptySet());
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(MODEL_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_FAVORITOS_IDS, Collections.emptySet());
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        }

        return "inicio";
    }

    /**
     * Página de login (estática)
     */
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (getToken(session) != null) {
            return "redirect:/detalle";
        }
        return "login";
    }

    /**
     * Página de detalles de recetas (protegida) - muestra favoritos del usuario
     */
    @GetMapping("/detalle")
    public String detalle(HttpSession session, Model model) {
        String token = getToken(session);
        String username = getUsername(session);

        if (token == null || username == null) {
            return "redirect:/login";
        }

        model.addAttribute(MODEL_USERNAME, username);
        model.addAttribute(MODEL_AUTHENTICATED, true);

        try {
            HttpResponse<String> responseFavs = backendApi.get("/api/usuarios/favoritos", token);
            model.addAttribute(MODEL_RECETAS, responseFavs.statusCode() == 200
                    ? backendApi.parseListResponse(responseFavs.body())
                    : Collections.emptyList());

            HttpResponse<String> responseMis = backendApi.get("/recipes/mis-recetas", token);
            model.addAttribute(MODEL_MIS_RECETAS, responseMis.statusCode() == 200
                    ? backendApi.parseListResponse(responseMis.body())
                    : Collections.emptyList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            model.addAttribute(MODEL_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_MIS_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(MODEL_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_MIS_RECETAS, Collections.emptyList());
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        }

        return "detalle";
    }

    /**
     * Página crear receta (protegida)
     */
    @GetMapping("/crear-receta")
    public String crearReceta(HttpSession session, Model model) {
        String token = getToken(session);
        String username = getUsername(session);

        if (token == null || username == null) {
            return "redirect:/login";
        }

        model.addAttribute(MODEL_USERNAME, username);
        model.addAttribute(MODEL_AUTHENTICATED, true);
        model.addAttribute("apiToken", token);

        return "crear-receta";
    }

    /**
     * Endpoint para ver el detalle completo de la receta
     */
    @GetMapping("/receta/{id}")
    public String verReceta(@PathVariable Long id, Model model, HttpSession session) {
        addAuthToModel(model, session);
        String token = getToken(session);
        if (token != null) {
            model.addAttribute("apiToken", token);
        }

        try {
            HttpResponse<String> response = backendApi.get("/recipes/" + id, null);
            if (response.statusCode() == 200) {
                model.addAttribute("receta", backendApi.parseMapResponse(response.body()));
            } else {
                model.addAttribute(MODEL_ERROR, "No se encontró la receta");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            model.addAttribute(MODEL_ERROR, MSG_ERROR_CONEXION + e.getMessage());
        }

        return "receta-detalle";
    }

    // ========================================================================
    // Endpoints proxy — API REST
    // ========================================================================

    /**
     * Endpoint proxy para creación de receta multimedial
     */
    @PostMapping(value = "/api/recipes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> crearRecetaProxy(
            @RequestParam("receta") String recetaJson,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos,
            HttpSession session) {

        String token = getToken(session);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MSG_NO_AUTORIZADO);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", token);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("receta", recetaJson);

            addFilesToBody(body, "imagenes", imagenes);
            addFilesToBody(body, "videos", videos);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    backendApi.getBackendUrl() + "/recipes", requestEntity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error de proxy frontend interno: " + e.getMessage());
        }
    }

    /** Helper para agregar archivos multipart al body. */
    private void addFilesToBody(MultiValueMap<String, Object> body, String fieldName, MultipartFile[] files) throws java.io.IOException {
        if (files == null) return;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                body.add(fieldName, new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }
        }
    }

    /**
     * Endpoint proxy para cambiar estado de receta
     */
    @PutMapping("/api/recipes/{id}/estado")
    @ResponseBody
    public ResponseEntity<String> cambiarEstadoProxy(
            @PathVariable("id") Long id,
            @RequestParam("publicada") boolean publicada,
            HttpSession session) {

        String token = getToken(session);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MSG_NO_AUTORIZADO);
        }

        try {
            HttpResponse<String> response = backendApi.put(
                    "/recipes/" + id + "/estado?publicada=" + publicada, token);
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MSG_ERROR_CONEXION + e.getMessage());
        }
    }

    /**
     * Endpoint para manejar login via proxy al backend
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginPost(
            @RequestBody Map<String, String> loginData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            Map<String, String> backendData = new HashMap<>();
            backendData.put("username", username);
            backendData.put("password", password);

            HttpResponse<String> backendResponse = backendApi.postJson(
                    "/login", null, backendApi.toJson(backendData));

            if (backendResponse.statusCode() == 200) {
                String token = backendResponse.body().trim();
                if (token != null && token.startsWith("Bearer ")) {
                    session.setAttribute(SESSION_JWT, token);
                    session.setAttribute(SESSION_USER, username);
                    response.put(KEY_SUCCESS, true);
                    response.put(KEY_MESSAGE, "Login exitoso");
                    response.put("redirectUrl", "/detalle");
                } else {
                    response.put(KEY_SUCCESS, false);
                    response.put(KEY_MESSAGE, "Token inválido recibido del backend");
                }
            } else {
                response.put(KEY_SUCCESS, false);
                response.put(KEY_MESSAGE, "Credenciales incorrectas");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.put(KEY_SUCCESS, false);
            response.put(KEY_MESSAGE, MSG_ERROR_CONEXION + e.getMessage());
        } catch (Exception e) {
            response.put(KEY_SUCCESS, false);
            response.put(KEY_MESSAGE, MSG_ERROR_CONEXION + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para manejar logout
     */
    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.removeAttribute(SESSION_JWT);
        session.removeAttribute(SESSION_USER);
        session.invalidate();

        Map<String, Object> response = new HashMap<>();
        response.put(KEY_SUCCESS, true);
        response.put(KEY_MESSAGE, "Logout exitoso");
        response.put("redirectUrl", "/login");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar autenticación
     */
    @PostMapping("/api/check-auth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String token = getToken(session);
        String username = getUsername(session);

        if (token != null && username != null) {
            response.put(MODEL_AUTHENTICATED, true);
            response.put(MODEL_USERNAME, username);
        } else {
            response.put(MODEL_AUTHENTICATED, false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para agregar receta a favoritos
     */
    @PostMapping("/api/favoritos/{recetaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarFavorito(
            @PathVariable Long recetaId, HttpSession session) {
        return toggleFavorito(recetaId, session, true);
    }

    /**
     * Endpoint para quitar receta de favoritos
     */
    @DeleteMapping("/api/favoritos/{recetaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quitarFavorito(
            @PathVariable Long recetaId, HttpSession session) {
        return toggleFavorito(recetaId, session, false);
    }

    /**
     * Endpoint para obtener recetas favoritas del usuario
     */
    @GetMapping("/api/favoritos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerFavoritos(HttpSession session) {
        String token = getToken(session);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        try {
            HttpResponse<String> backendResponse = backendApi.get("/api/usuarios/favoritos", token);
            if (backendResponse.statusCode() == 200) {
                return ResponseEntity.ok(backendApi.parseListResponse(backendResponse.body()));
            }
            return ResponseEntity.ok(Collections.emptyList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // ========================================================================
    // Proxies — Comentarios y Valoraciones (usando helpers genéricos)
    // ========================================================================

    @GetMapping("/api/recipes/{id}/comentarios")
    @ResponseBody
    public ResponseEntity<String> getComentarios(@PathVariable Long id) {
        return proxyGet("/recipes/" + id + "/comentarios", null);
    }

    @PostMapping("/api/recipes/{id}/comentarios")
    @ResponseBody
    public ResponseEntity<String> postComentario(
            @PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        return proxyPostAuth("/recipes/" + id + "/comentarios", body, session);
    }

    @GetMapping("/api/recipes/{id}/valoraciones")
    @ResponseBody
    public ResponseEntity<String> getValoraciones(@PathVariable Long id, HttpSession session) {
        return proxyGet("/recipes/" + id + "/valoraciones", getToken(session));
    }

    @PostMapping("/api/recipes/{id}/valoraciones")
    @ResponseBody
    public ResponseEntity<String> postValoracion(
            @PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        return proxyPostAuth("/recipes/" + id + "/valoraciones", body, session);
    }
}