package com.david.ProyectoFinal.dto;

public class TiendaSimpleDTO {

    private Long id;
    private String nombre;
    private String url;

    public TiendaSimpleDTO() {
    }

    public TiendaSimpleDTO(Long id, String nombre, String url) {
        this.id = id;
        this.nombre = nombre;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUrl() {
        return url;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}