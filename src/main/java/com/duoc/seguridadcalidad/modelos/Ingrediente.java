package com.duoc.seguridadcalidad.modelos;

import jakarta.persistence.*;

@Embeddable
public class Ingrediente {

    private String nombre;
    private Double cantidad;
    private String unidadMedida;

    public Ingrediente() {
    }

    public Ingrediente(String nombre, Double cantidad, String unidadMedida) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    @Override
    public String toString() {
        return "Ingrediente{" +
                "nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                ", unidadMedida='" + unidadMedida + '\'' +
                '}';
    }
}
