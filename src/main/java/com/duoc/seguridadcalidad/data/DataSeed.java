package com.duoc.seguridadcalidad.data;


import com.duoc.seguridadcalidad.modelos.Ingrediente;
import com.duoc.seguridadcalidad.modelos.Receta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.duoc.seguridadcalidad.modelos.Dificultad.MEDIA;

public class DataSeed {
    public static List<Receta> generarRecetas() {
        List<Receta> lista = new ArrayList<>();

        // Creamos la lista de imágenes
        List<String> imagenes = new ArrayList<>();
        imagenes.add("url.imagen1.net");
        imagenes.add("url.imagen2.net");

        // Agregamos la receta usando el constructor que definimos
        lista.add(new Receta(
                1,
                "Tacos al Pastor",
                "Urbana",
                "México",
                MEDIA,
                50,
                "Marinar la carne y asar...",
                4.5,
                LocalDate.of(2025,12,17),
                imagenes,
                new Ingrediente("Cerdo", 1.0, "kg"),
                new Ingrediente("Piña", 0.5, "pza")
        ));
        return lista;
    }
}
