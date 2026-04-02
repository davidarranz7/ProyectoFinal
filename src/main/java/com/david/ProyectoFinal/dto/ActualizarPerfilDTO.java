package com.david.ProyectoFinal.dto;

public class ActualizarPerfilDTO {

    private String nombre;
    private String email;

    public ActualizarPerfilDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}