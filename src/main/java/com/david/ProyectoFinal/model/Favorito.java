package com.david.ProyectoFinal.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity/// Convierte la clase en tabla
@Table(
        name = "favoritos",/// nombre de la Tabla
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "producto_id"})
)
public class Favorito {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    @ManyToOne/// Muchos favoritos pueden pertenecer a un usuario
    @JoinColumn(name = "usuario_id", nullable = false)/// Nombre de la columna en la tabla y no puede ser nulo
    private Usuario usuario;

    @ManyToOne/// Muchos favoritos pueden pertenecer a un producto
    @JoinColumn(name = "producto_id", nullable = false)/// Nombre de la columna en la tabla y no puede ser nulo
    private Producto producto;

    private LocalDateTime fechaAgregado;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos

    public Favorito() {
    }

    /// Constructor con parámetros-> Todos los necesarios de un Favorito

    public Favorito(Long id, Usuario usuario, Producto producto, LocalDateTime fechaAgregado) {
        this.id = id;
        this.usuario = usuario;
        this.producto = producto;
        this.fechaAgregado = fechaAgregado;
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

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}
