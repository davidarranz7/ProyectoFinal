package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_imagenes")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_imagen", length = 1000)
    private String urlImagen;

    private int orden;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    public ProductoImagen() {
    }

    public ProductoImagen(Long id, String urlImagen, int orden, Producto producto) {
        this.id = id;
        this.urlImagen = urlImagen;
        this.orden = orden;
        this.producto = producto;
    }

    public Long getId() {
        return id;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}