package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity/// Convierte la clase en tabla
@Table(name = "items_carrito")/// nombre de la Tabla
public class ItemCarrito {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    @ManyToOne/// Indica que un carrito puede tener muchos items, pero cada item pertenece a un solo carrito
    private Carrito carrito;

    @ManyToOne/// Indica que un producto puede estar en muchos items, pero cada item se refiere a un solo producto
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
