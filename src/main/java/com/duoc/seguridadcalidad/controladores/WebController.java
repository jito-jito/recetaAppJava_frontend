package com.duoc.seguridadcalidad.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {};
                
                java.util.List<java.util.Map<String, Object>> recetas = objectMapper.readValue(response.body(), typeRef);
                model.addAttribute("recetas", recetas);
            } else {
                model.addAttribute("recetas", java.util.Collections.emptyList());
                model.addAttribute("error", "No se pudieron cargar las recetas del backend");
            }
        } catch (Exception e) {
            // En caso de error, mostrar lista vacía
            model.addAttribute("recetas", java.util.Collections.emptyList());
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
            // Obtener recetas favoritas del usuario
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BACKEND_URL + "/api/usuarios/favoritos"))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parsear las recetas favoritas
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {};
                
                java.util.List<java.util.Map<String, Object>> favoritos = objectMapper.readValue(response.body(), typeRef);
                model.addAttribute("recetas", favoritos);
            } else {
                model.addAttribute("recetas", java.util.Collections.emptyList());
                model.addAttribute("error", "No se pudieron cargar las recetas favoritas");
            }
        } catch (Exception e) {
            // En caso de error, mostrar lista vacía
            model.addAttribute("recetas", java.util.Collections.emptyList());
            model.addAttribute("error", "Error de conexión con el backend: " + e.getMessage());
        }
        
        return "detalle";
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
                // El backend retorna el token directamente en el body como texto "Bearer eyJ..."
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
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {};
                
                java.util.List<java.util.Map<String, Object>> favoritos = objectMapper.readValue(backendResponse.body(), typeRef);
                return ResponseEntity.ok(favoritos);
            } else {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
}