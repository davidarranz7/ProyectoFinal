package com.david.ProyectoFinal.dto;

public class UsuarioPerfilDTO {

    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private String fotoPerfilUrl;
    private String formaFotoPerfil;

    public UsuarioPerfilDTO() {
    }

    public UsuarioPerfilDTO(Long id, String nombre, String email, String rol, String fotoPerfilUrl, String formaFotoPerfil) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.formaFotoPerfil = formaFotoPerfil;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getFormaFotoPerfil() {
        return formaFotoPerfil;
    }

    public void setFormaFotoPerfil(String formaFotoPerfil) {
        this.formaFotoPerfil = formaFotoPerfil;
    }
}