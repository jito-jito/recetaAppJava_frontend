package com.duoc.seguridadcalidad.modelos;

public class BannerPublicitario {

    private Integer idBanner;
    private String marca; //anunciante
    private String urlImagen;
    private String urlDestino; //a donde redirige
    private String ubicacion; //header, sidebar, footer

    public BannerPublicitario(){

    }

    public BannerPublicitario(Integer idBanner, String marca, String urlImagen, String urlDestino, String ubicacion) {
        this.idBanner = idBanner;
        this.marca = marca;
        this.urlImagen = urlImagen;
        this.urlDestino = urlDestino;
        this.ubicacion = ubicacion;
    }

    public Integer getIdBanner() {
        return idBanner;
    }

    public void setIdBanner(Integer idBanner) {
        this.idBanner = idBanner;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getUrlDestino() {
        return urlDestino;
    }

    public void setUrlDestino(String urlDestino) {
        this.urlDestino = urlDestino;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }


}
