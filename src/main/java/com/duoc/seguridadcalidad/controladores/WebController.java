package com.duoc.seguridadcalidad.controladores;

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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controlador web para manejar las páginas del frontend y conexión con backend
 */
@Controller
public class WebController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BACKEND_URL = "http://localhost:8080";

    /**
     * Página principal
     */
    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {
        // Verificar si hay sesión activa
        String jwtToken = (String) session.getAttribute("jwtToken");
        String username = (String) session.getAttribute("username");

        if (jwtToken != null && username != null) {
            // Usuario autenticado
            model.addAttribute("authenticated", true);
            model.addAttribute("username", username);
        } else {
            // Usuario no autenticado
            model.addAttribute("authenticated", false);
        }

        try {
            // Obtener recetas del backend
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/recipes"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parsear las recetas usando ObjectMapper
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {
                };

                java.util.List<java.util.Map<String, Object>> recetas = objectMapper.readValue(response.body(),
                        typeRef);
                model.addAttribute("recetas", recetas);

                // Si el usuario está autenticado, también obtener sus favoritos
                if (jwtToken != null) {
                    try {
                        HttpRequest favoritosRequest = HttpRequest.newBuilder()
                                .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos"))
                                .header("Accept", "application/json")
                                .header("Authorization", jwtToken)
                                .GET()
                                .build();

                        HttpResponse<String> favoritosResponse = client.send(favoritosRequest,
                                HttpResponse.BodyHandlers.ofString());

                        if (favoritosResponse.statusCode() == 200) {
                            // Parsear favoritos y crear lista de IDs
                            java.util.List<java.util.Map<String, Object>> favoritos = objectMapper
                                    .readValue(favoritosResponse.body(), typeRef);
                            java.util.Set<Object> favoritosIds = new java.util.HashSet<>();

                            for (java.util.Map<String, Object> favorito : favoritos) {
                                favoritosIds.add(favorito.get("idReceta"));
                            }

                            model.addAttribute("favoritosIds", favoritosIds);
                        } else {
                            model.addAttribute("favoritosIds", java.util.Collections.emptySet());
                        }
                    } catch (Exception e) {
                        // Si hay error obteniendo favoritos, continuar sin ellos
                        model.addAttribute("favoritosIds", java.util.Collections.emptySet());
                    }
                } else {
                    model.addAttribute("favoritosIds", java.util.Collections.emptySet());
                }

            } else {
                model.addAttribute("recetas", java.util.Collections.emptyList());
                model.addAttribute("favoritosIds", java.util.Collections.emptySet());
                model.addAttribute("error", "No se pudieron cargar las recetas del backend");
            }
        } catch (Exception e) {
            // En caso de error, mostrar lista vacía
            model.addAttribute("recetas", java.util.Collections.emptyList());
            model.addAttribute("favoritosIds", java.util.Collections.emptySet());
            model.addAttribute("error", "Error de conexión con el backend: " + e.getMessage());
        }

        return "inicio";
    }

    /**
     * Página de login (estática)
     */
    @GetMapping("/login")
    public String login(HttpSession session) {
        // Si ya está logueado, redireccionar a detalle

        if (session.getAttribute("jwtToken") != null) {
            return "redirect:/detalle";
        }
        return "login";
    }

    /**
     * Página de detalles de recetas (protegida) - muestra favoritos del usuario
     */
    @GetMapping("/detalle")
    public String detalle(HttpSession session, Model model) {
        // Verificar autenticación
        String token = (String) session.getAttribute("jwtToken");
        String username = (String) session.getAttribute("username");

        if (token == null || username == null) {
            // No está autenticado, redireccionar a login
            return "redirect:/login";
        }

        // Agregar información del usuario al modelo
        model.addAttribute("username", username);
        model.addAttribute("authenticated", true);

        try {
            HttpClient client = HttpClient.newHttpClient();
            com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {};

            // 1. Obtener recetas favoritas del usuario
            HttpRequest requestFavs = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos"))
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<String> responseFavs = client.send(requestFavs, HttpResponse.BodyHandlers.ofString());
            if (responseFavs.statusCode() == 200) {
                java.util.List<java.util.Map<String, Object>> favoritos = objectMapper.readValue(responseFavs.body(), typeRef);
                model.addAttribute("recetas", favoritos);
            } else {
                model.addAttribute("recetas", java.util.Collections.emptyList());
                model.addAttribute("errorFav", "No se pudieron cargar las recetas favoritas");
            }

            // 2. Obtener recetas creadas por el usuario (Borradores y Publicadas)
            HttpRequest requestMisRecetas = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/recipes/mis-recetas"))
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<String> responseMis = client.send(requestMisRecetas, HttpResponse.BodyHandlers.ofString());
            if (responseMis.statusCode() == 200) {
                java.util.List<java.util.Map<String, Object>> misRecetas = objectMapper.readValue(responseMis.body(), typeRef);
                model.addAttribute("misRecetas", misRecetas);
            } else {
                model.addAttribute("misRecetas", java.util.Collections.emptyList());
                model.addAttribute("errorMis", "No se pudieron cargar tus recetas creadas");
            }

        } catch (Exception e) {
            // En caso de error, mostrar listas vacías
            model.addAttribute("recetas", java.util.Collections.emptyList());
            model.addAttribute("misRecetas", java.util.Collections.emptyList());
            model.addAttribute("error", "Error de conexión con el backend: " + e.getMessage());
        }

        return "detalle";
    }

    /**
     * Página crear receta (protegida)
     */
    @GetMapping("/crear-receta")
    public String crearReceta(HttpSession session, Model model) {
        String token = (String) session.getAttribute("jwtToken");
        String username = (String) session.getAttribute("username");

        if (token == null || username == null) {
            return "redirect:/login"; // No está autenticado, redireccionar a login
        }

        model.addAttribute("username", username);
        model.addAttribute("authenticated", true);
        model.addAttribute("apiToken", token); // Inyectamos el JWT de sesión para el Javascript en la web

        return "crear-receta";
    }

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

        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", token);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("receta", recetaJson);

            if (imagenes != null) {
                for (MultipartFile file : imagenes) {
                    if (!file.isEmpty()) {
                        body.add("imagenes", new ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() {
                                return file.getOriginalFilename();
                            }
                        });
                    }
                }
            }

            if (videos != null) {
                for (MultipartFile file : videos) {
                    if (!file.isEmpty()) {
                        body.add("videos", new ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() {
                                return file.getOriginalFilename();
                            }
                        });
                    }
                }
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BACKEND_URL + "/recipes", requestEntity, String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error de proxy frontend interno: " + e.getMessage());
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

        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/recipes/" + id + "/estado?publicada=" + publicada))
                    .header("Authorization", token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode()).body(response.body());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error de proxy en el frontend: " + e.getMessage());
        }
    }

    /**
     * Endpoint para manejar login via proxy al backend
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            // Crear cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Preparar datos para enviar al backend
            Map<String, String> backendData = new HashMap<>();
            backendData.put("username", username);
            backendData.put("password", password);

            String requestBody = objectMapper.writeValueAsString(backendData);

            // Crear petición al backend
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Enviar petición
            HttpResponse<String> backendResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (backendResponse.statusCode() == 200) {
                // El backend retorna el token directamente en el body como texto "Bearer
                // eyJ..."
                String token = backendResponse.body().trim();

                if (token != null && token.startsWith("Bearer ")) {
                    // Guardar el token completo en sesión (ya incluye "Bearer ")
                    session.setAttribute("jwtToken", token);
                    session.setAttribute("username", username);

                    response.put("success", true);
                    response.put("message", "Login exitoso");
                    response.put("redirectUrl", "/detalle");
                } else {
                    response.put("success", false);
                    response.put("message", "Token inválido recibido del backend");
                }
            } else {
                response.put("success", false);
                response.put("message", "Credenciales incorrectas");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error de conexión con el backend: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para manejar logout
     */
    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Limpiar sesión
        session.removeAttribute("jwtToken");
        session.removeAttribute("username");
        session.invalidate();

        response.put("success", true);
        response.put("message", "Logout exitoso");
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

        String token = (String) session.getAttribute("jwtToken");
        String username = (String) session.getAttribute("username");

        if (token != null && username != null) {
            response.put("authenticated", true);
            response.put("username", username);
            // temporal
            response.put("debug_token", token);
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para agregar receta a favoritos
     */
    @PostMapping("/api/favoritos/{recetaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarFavorito(@PathVariable Long recetaId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Verificar autenticación
        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            response.put("success", false);
            response.put("message", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // Hacer petición al backend
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos/" + recetaId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> backendResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (backendResponse.statusCode() == 200) {
                response.put("success", true);
                response.put("message", "Receta agregada a favoritos exitosamente");
                response.put("recetaId", recetaId);
            } else {
                response.put("success", false);
                response.put("message", "Error al agregar a favoritos");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error de conexión: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para quitar receta de favoritos
     */
    @DeleteMapping("/api/favoritos/{recetaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quitarFavorito(@PathVariable Long recetaId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Verificar autenticación
        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            response.put("success", false);
            response.put("message", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // Hacer petición al backend
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos/" + recetaId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", token)
                    .DELETE()
                    .build();

            HttpResponse<String> backendResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (backendResponse.statusCode() == 200) {
                response.put("success", true);
                response.put("message", "Receta quitada de favoritos exitosamente");
                response.put("recetaId", recetaId);
            } else {
                response.put("success", false);
                response.put("message", "Error al quitar de favoritos");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error de conexión: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener recetas favoritas del usuario
     */
    @GetMapping("/api/favoritos")
    @ResponseBody
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> obtenerFavoritos(HttpSession session) {
        // Verificar autenticación
        String token = (String) session.getAttribute("jwtToken");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Collections.emptyList());
        }

        try {
            // Hacer petición al backend
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos"))
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<String> backendResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (backendResponse.statusCode() == 200) {
                // Parsear las recetas favoritas
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {
                };

                java.util.List<java.util.Map<String, Object>> favoritos = objectMapper.readValue(backendResponse.body(),
                        typeRef);
                return ResponseEntity.ok(favoritos);
            } else {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
}