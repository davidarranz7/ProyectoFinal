package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity/// Convierte la clase en tabla
@Table(name = "productos")/// nombre de la Tabla
public class Producto {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    private String nombre;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private BigDecimal precio;///PAra poder evvitar probelmas de redondeo
    private String urlImagen;
    private String urlProducto;

    @Enumerated(EnumType.STRING)
    private Seccion seccion;

    @ManyToOne/// Muchos productos pueden pertenecer a una categoría
    private Categoria categoria;

    @ManyToOne/// Muchos productos pueden pertenecer a una tienda
    private Tienda tienda;


    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos
    public Producto() {
    }

    /// Constructor con parámetros-> Todos los necesarios de un Producto
    public Producto(Long id, String nombre, String descripcion, BigDecimal precio, String urlImagen, String urlProducto, Seccion seccion, Categoria categoria, Tienda tienda) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.urlImagen = urlImagen;
        this.urlProducto = urlProducto;
        this.seccion = seccion;
        this.categoria = categoria;
        this.tienda = tienda;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getUrlProducto() {
        return urlProducto;
    }

    public void setUrlProducto(String urlProducto) {
        this.urlProducto = urlProducto;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public void setTienda(Tienda tienda) {
        this.tienda = tienda;
    }
}
