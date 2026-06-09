package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(precision = 10, scale = 2)
    private BigDecimal precioOriginal;

    private Integer porcentajeDescuento;

    @Column(nullable = false)
    private Boolean enOferta = false;

    private String urlImagen;

    private String urlProducto;

    @Enumerated(EnumType.STRING)
    private Seccion seccion;

    @ManyToOne/// Muchos productos pueden pertenecer a una categoría
    private Categoria categoria;

    @ManyToOne/// Muchos productos pueden pertenecer a una tienda
    private Tienda tienda;

    @Column(name = "nueva_coleccion")
    private Boolean nuevaColeccion = false;

    @Column(name = "disponible_catalogo")
    private Boolean disponibleCatalogo = true;

    private LocalDateTime ultimaVezVistoEnScraping;

    private LocalDateTime fechaDesactivacion;

    @Column(length = 120)
    private String motivoDesactivacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)/// Un producto puede tener muchas tallas y stock
    private List<ProductoTallaStock> tallaStocks = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ProductoImagen> imagenes = new ArrayList<>();

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos
    public Producto() {
    }

    /// Constructor con parámetros-> Todos los necesarios de un Producto
    public Producto(Long id, String nombre, String descripcion, BigDecimal precio, BigDecimal precioOriginal, Integer porcentajeDescuento, Boolean enOferta, String urlImagen, String urlProducto, Seccion seccion, Categoria categoria, Tienda tienda, List<ProductoTallaStock> tallaStocks, List<ProductoImagen> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioOriginal = precioOriginal;
        this.porcentajeDescuento = porcentajeDescuento;
        this.enOferta = enOferta != null ? enOferta : false;
        this.urlImagen = urlImagen;
        this.urlProducto = urlProducto;
        this.seccion = seccion;
        this.categoria = categoria;
        this.tienda = tienda;
        this.tallaStocks = tallaStocks != null ? tallaStocks : new ArrayList<>();
        this.imagenes = new ArrayList<>();

        if (imagenes != null) {
            for (ProductoImagen imagen : imagenes) {
                addImagen(imagen);
            }
        }
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

    public List<ProductoTallaStock> getTallaStocks() {
        return tallaStocks;
    }

    public void setTallaStocks(List<ProductoTallaStock> tallaStocks) {
        this.tallaStocks = tallaStocks != null ? tallaStocks : new ArrayList<>();
    }

    public List<ProductoImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ProductoImagen> imagenes) {
        this.imagenes.clear();

        if (imagenes != null) {
            for (ProductoImagen imagen : imagenes) {
                addImagen(imagen);
            }
        }
    }

    public void addImagen(ProductoImagen imagen) {
        this.imagenes.add(imagen);
        imagen.setProducto(this);
    }

    public BigDecimal getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(BigDecimal precioOriginal) {
        this.precioOriginal = precioOriginal;
    }

    public Integer getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Integer porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Boolean getEnOferta() {
        return enOferta;
    }

    public void setEnOferta(Boolean enOferta) {
        this.enOferta = enOferta != null ? enOferta : false;
    }

    public Boolean getNuevaColeccion() {
        return nuevaColeccion;
    }

    public void setNuevaColeccion(Boolean nuevaColeccion) {
        this.nuevaColeccion = nuevaColeccion;
    }

    public Boolean getDisponibleCatalogo() {
        return disponibleCatalogo;
    }

    public void setDisponibleCatalogo(Boolean disponibleCatalogo) {
        this.disponibleCatalogo = disponibleCatalogo;
    }

    public LocalDateTime getUltimaVezVistoEnScraping() {
        return ultimaVezVistoEnScraping;
    }

    public void setUltimaVezVistoEnScraping(LocalDateTime ultimaVezVistoEnScraping) {
        this.ultimaVezVistoEnScraping = ultimaVezVistoEnScraping;
    }

    public LocalDateTime getFechaDesactivacion() {
        return fechaDesactivacion;
    }

    public void setFechaDesactivacion(LocalDateTime fechaDesactivacion) {
        this.fechaDesactivacion = fechaDesactivacion;
    }

    public String getMotivoDesactivacion() {
        return motivoDesactivacion;
    }

    public void setMotivoDesactivacion(String motivoDesactivacion) {
        this.motivoDesactivacion = motivoDesactivacion;
    }
}
