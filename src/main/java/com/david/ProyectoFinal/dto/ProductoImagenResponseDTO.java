package com.david.ProyectoFinal.dto;

public class ProductoImagenResponseDTO {

    private Long id;
    private String urlImagen;
    private int orden;

    public ProductoImagenResponseDTO() {
    }

    public ProductoImagenResponseDTO(Long id, String urlImagen, int orden) {
        this.id = id;
        this.urlImagen = urlImagen;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public int getOrden() {
        return orden;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}