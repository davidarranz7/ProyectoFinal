package com.david.ProyectoFinal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity/// Convierte la clase en tabla
@Table (name = "usuarios")/// nombre de la Tabla
public class Usuario {

    ///Atributos

    @Id/// Indica que es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    private String nombre;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)/// Evita que la contraseña se muestre en las respuestas JSON
    private String password;


    @Enumerated(EnumType.STRING)
    private Rol rol;

    private String fotoPerfilUrl;
    private String formaFotoPerfil;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos

    public Usuario() {
    }
    /// Constructor con parámetros-> Todos los necesarios de un usuario
    public Usuario(Long id, String nombre, String email, String password, Rol rol, String fotoPerfilUrl, String formaFotoPerfil) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.formaFotoPerfil = formaFotoPerfil;
    }

    /// Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
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
