package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_talla_stock")/// nombre de la tabla en la base de datos
public class ProductoTallaStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)/// clave foranea
    private Producto producto;

    @Enumerated(EnumType.STRING)
    private Talla talla;

    private Integer stock;

    public ProductoTallaStock() {
    }

    public ProductoTallaStock(Long id, Producto producto, Talla talla, Integer stock) {
        this.id = id;
        this.producto = producto;
        this.talla = talla;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
