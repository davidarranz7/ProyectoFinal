package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity/// Convierte la clase en tabla
@Table(name = "items_carrito",/// nombre de la Tabla
        uniqueConstraints = @UniqueConstraint(columnNames = {"carrito_id", "producto_id"}))/// Evita que un mismo producto se agregue varias veces al mismo carrito, cada combinación de carrito y producto debe ser única
public class ItemCarrito {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    @ManyToOne/// Indica que un carrito puede tener muchos items, pero cada item pertenece a un solo carrito
    @JoinColumn(name = "carrito_id", nullable = false)/// Clave foránea que referencia al carrito, no puede ser nula
    private Carrito carrito;

    @ManyToOne/// Indica que un producto puede estar en muchos items, pero cada item se refiere a un solo producto
    @JoinColumn(name = "producto_id", nullable = false)/// Clave foránea que referencia al producto, no puede ser nula
    private Producto producto;

    private Integer cantidad;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos

    public ItemCarrito() {
    }

    /// Constructor con parámetros-> Todos los necesarios de un ItemCarrito

    public ItemCarrito(Long id, Carrito carrito, Producto producto, Integer cantidad) {
        this.id = id;
        this.carrito = carrito;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    /// Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
