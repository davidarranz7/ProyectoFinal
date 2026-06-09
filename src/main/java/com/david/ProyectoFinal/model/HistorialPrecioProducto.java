package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_precios_producto")
public class HistorialPrecioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scraping_ejecucion_id")
    private ScrapingEjecucion scrapingEjecucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TipoCambioPrecio tipoCambio;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    private String nombreProducto;

    private String tienda;

    @Column(columnDefinition = "TEXT")
    private String urlProducto;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioOriginalAnterior;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioOriginalNuevo;

    private Integer porcentajeDescuentoAnterior;

    private Integer porcentajeDescuentoNuevo;

    @Column(precision = 7, scale = 2)
    private BigDecimal porcentajeVariacionPrecio;

    @Column(nullable = false)
    private Boolean rebajaMayor = false;

    public HistorialPrecioProducto() {
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

    public ScrapingEjecucion getScrapingEjecucion() {
        return scrapingEjecucion;
    }

    public void setScrapingEjecucion(ScrapingEjecucion scrapingEjecucion) {
        this.scrapingEjecucion = scrapingEjecucion;
    }

    public TipoCambioPrecio getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(TipoCambioPrecio tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public String getUrlProducto() {
        return urlProducto;
    }

    public void setUrlProducto(String urlProducto) {
        this.urlProducto = urlProducto;
    }

    public BigDecimal getPrecioAnterior() {
        return precioAnterior;
    }

    public void setPrecioAnterior(BigDecimal precioAnterior) {
        this.precioAnterior = precioAnterior;
    }

    public BigDecimal getPrecioNuevo() {
        return precioNuevo;
    }

    public void setPrecioNuevo(BigDecimal precioNuevo) {
        this.precioNuevo = precioNuevo;
    }

    public BigDecimal getPrecioOriginalAnterior() {
        return precioOriginalAnterior;
    }

    public void setPrecioOriginalAnterior(BigDecimal precioOriginalAnterior) {
        this.precioOriginalAnterior = precioOriginalAnterior;
    }

    public BigDecimal getPrecioOriginalNuevo() {
        return precioOriginalNuevo;
    }

    public void setPrecioOriginalNuevo(BigDecimal precioOriginalNuevo) {
        this.precioOriginalNuevo = precioOriginalNuevo;
    }

    public Integer getPorcentajeDescuentoAnterior() {
        return porcentajeDescuentoAnterior;
    }

    public void setPorcentajeDescuentoAnterior(Integer porcentajeDescuentoAnterior) {
        this.porcentajeDescuentoAnterior = porcentajeDescuentoAnterior;
    }

    public Integer getPorcentajeDescuentoNuevo() {
        return porcentajeDescuentoNuevo;
    }

    public void setPorcentajeDescuentoNuevo(Integer porcentajeDescuentoNuevo) {
        this.porcentajeDescuentoNuevo = porcentajeDescuentoNuevo;
    }

    public BigDecimal getPorcentajeVariacionPrecio() {
        return porcentajeVariacionPrecio;
    }

    public void setPorcentajeVariacionPrecio(BigDecimal porcentajeVariacionPrecio) {
        this.porcentajeVariacionPrecio = porcentajeVariacionPrecio;
    }

    public Boolean getRebajaMayor() {
        return rebajaMayor;
    }

    public void setRebajaMayor(Boolean rebajaMayor) {
        this.rebajaMayor = rebajaMayor;
    }
}
