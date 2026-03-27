package com.duoc.seguridadcalidad.controladores;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duoc.seguridadcalidad.modelos.Receta;
import com.duoc.seguridadcalidad.repositorios.RecetaRepository;

@RestController
@RequestMapping("/recipes")
@CrossOrigin(origins = "*") // Para permitir peticiones desde el frontend
public class RecipeController {

    @Autowired
    private RecetaRepository recetaRepository;

    @GetMapping
    public Iterable<Receta> getAllRecipes() {
        return recetaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Receta> getRecipeById(@PathVariable("id") Integer id) {
        Optional<Receta> receta = recetaRepository.findById(id);
        return receta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Iterable<Receta> searchRecipes(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String tipoCocina,
            @RequestParam(required = false) String paisOrigen,
            @RequestParam(required = false) Integer tiempoMaximo,
            @RequestParam(required = false) Double popularidadMinima
    ) {
        
        if (titulo != null && !titulo.isBlank()) {
            return recetaRepository.findByTituloContainingIgnoreCase(titulo);
        }
        if (tipoCocina != null && !tipoCocina.isBlank()) {
            return recetaRepository.findByTipoCocinaContainingIgnoreCase(tipoCocina);
        }
        if (paisOrigen != null && !paisOrigen.isBlank()) {
            return recetaRepository.findByPaisOrigenContainingIgnoreCase(paisOrigen);
        }
        if (tiempoMaximo != null) {
            return recetaRepository.findByTiempoCoccionLessThanEqual(tiempoMaximo);
        }
        if (popularidadMinima != null) {
            return recetaRepository.findByPopularidadGreaterThanEqual(popularidadMinima);
        }
        
        return recetaRepository.findAll();
    }

    @GetMapping("/populares")
    public List<Receta> getRecipesByPopularity() {
        return recetaRepository.findAllByOrderByPopularidadDesc();
    }

    @GetMapping("/recientes")
    public List<Receta> getRecentRecipes() {
        return recetaRepository.findAllByOrderByFechaPublicacionDesc();
    }

    @PostMapping
    public ResponseEntity<Receta> createRecipe(@RequestBody Receta receta) {
        if (receta.getIdReceta() != null) {
            // Avoid allowing the client to set the id manually when creating.
            receta.setIdReceta(null);
        }
        Receta saved = recetaRepository.save(receta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Receta> updateRecipe(@PathVariable("id") Integer id, @RequestBody Receta receta) {
        if (!recetaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        receta.setIdReceta(id);
        Receta updated = recetaRepository.save(receta);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") Integer id) {
        if (!recetaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        recetaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}