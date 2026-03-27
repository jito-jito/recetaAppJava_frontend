package com.duoc.seguridadcalidad.controladores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController 
public class HomeController { 

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API Backend de Recetas - Seguridad y Calidad en el Desarrollo");
        response.put("version", "1.0.0");
        response.put("status", "active");
        response.put("endpoints", Map.of(
            "recipes", "/recipes",
            "login", "/login",
            "h2-console", "/h2-console (development only)"
        ));
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "receta-backend-api");
        return response;
    }
}

