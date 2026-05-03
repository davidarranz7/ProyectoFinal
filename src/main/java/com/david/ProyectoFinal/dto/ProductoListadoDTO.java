package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Seccion;

import java.math.BigDecimal;
import java.util.List;

public class ProductoListadoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String urlImagen;
    private String urlProducto;
    private Seccion seccion;
    private CategoriaSimpleDTO categoria;
    private TiendaSimpleDTO tienda;
    private List<ProductoTallaStockResponseDTO> tallaStocks;
    private List<ProductoImagenResponseDTO> imagenes;

    public ProductoListadoDTO() {
    }

    public ProductoListadoDTO(Long id,
                              String nombre,
                              String descripcion,
                              BigDecimal precio,
                              String urlImagen,
                              String urlProducto,
                              Seccion seccion,
                              CategoriaSimpleDTO categoria,
                              TiendaSimpleDTO tienda,
                              List<ProductoTallaStockResponseDTO> tallaStocks) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.urlImagen = urlImagen;
        this.urlProducto = urlProducto;
        this.seccion = seccion;
        this.categoria = categoria;
        this.tienda = tienda;
        this.tallaStocks = tallaStocks;
    }

    public ProductoListadoDTO(Long id,
                              String nombre,
                              String descripcion,
                              BigDecimal precio,
                              String urlImagen,
                              String urlProducto,
                              Seccion seccion,
                              CategoriaSimpleDTO categoria,
                              TiendaSimpleDTO tienda,
                              List<ProductoTallaStockResponseDTO> tallaStocks,
                              List<ProductoImagenResponseDTO> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.urlImagen = urlImagen;
        this.urlProducto = urlProducto;
        this.seccion = seccion;
        this.categoria = categoria;
        this.tienda = tienda;
        this.tallaStocks = tallaStocks;
        this.imagenes = imagenes;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public String getUrlProducto() {
        return urlProducto;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public CategoriaSimpleDTO getCategoria() {
        return categoria;
    }

    public TiendaSimpleDTO getTienda() {
        return tienda;
    }

    public List<ProductoTallaStockResponseDTO> getTallaStocks() {
        return tallaStocks;
    }

    public List<ProductoImagenResponseDTO> getImagenes() {
        return imagenes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public void setUrlProducto(String urlProducto) {
        this.urlProducto = urlProducto;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
    }

    public void setCategoria(CategoriaSimpleDTO categoria) {
        this.categoria = categoria;
    }

    public void setTienda(TiendaSimpleDTO tienda) {
        this.tienda = tienda;
    }

    public void setTallaStocks(List<ProductoTallaStockResponseDTO> tallaStocks) {
        this.tallaStocks = tallaStocks;
    }

    public void setImagenes(List<ProductoImagenResponseDTO> imagenes) {
        this.imagenes = imagenes;
    }
}