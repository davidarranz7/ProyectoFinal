package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity/// Convierte la clase en tabla
@Table(name = "carritos")/// nombre de la Tabla///
public class Carrito {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    @OneToOne
    private Usuario usuario;

    private LocalDateTime fechaCreacion;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos

    public Carrito() {
    }

    /// Constructor con parámetros-> Todos los necesarios de un Carrito

    public Carrito(Long id, Usuario usuario, LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuario = usuario;
        this.fechaCreacion = fechaCreacion;
    }

    /// Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
