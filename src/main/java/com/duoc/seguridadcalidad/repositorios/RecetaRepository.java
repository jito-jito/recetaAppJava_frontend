package com.duoc.seguridadcalidad.repositorios;

import org.springframework.data.repository.CrudRepository;
import com.duoc.seguridadcalidad.modelos.Receta;
import com.duoc.seguridadcalidad.modelos.Dificultad;

import java.util.List;

public interface RecetaRepository extends CrudRepository<Receta, Integer> {

    List<Receta> findByTituloContainingIgnoreCase(String titulo);

    List<Receta> findByTipoCocinaContainingIgnoreCase(String tipoCocina);

    List<Receta> findByPaisOrigenContainingIgnoreCase(String paisOrigen);

    List<Receta> findByDificultad(Dificultad dificultad);

    List<Receta> findByTiempoCoccionLessThanEqual(Integer tiempoCoccion);

    List<Receta> findByPopularidadGreaterThanEqual(Double popularidad);

    List<Receta> findAllByOrderByFechaPublicacionDesc();

    List<Receta> findAllByOrderByPopularidadDesc();

}
