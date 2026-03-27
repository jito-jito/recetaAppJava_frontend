package com.duoc.seguridadcalidad.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador web para manejar las páginas del frontend
 */
@Controller
public class WebController {

    /**
     * Página principal
     */
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    /**
     * Página de login (estática)
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Página de detalles de recetas
     */
    @GetMapping("/detalle")
    public String detalle() {
        return "detalle";
    }
}