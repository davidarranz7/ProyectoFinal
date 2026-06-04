package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Seccion;

public class ProductoSeleccionStockDTO {

    private Long id;
    private String nombre;
    private Seccion seccion;
    private CategoriaSimpleDTO categoria;
    private TiendaSimpleDTO tienda;

    public ProductoSeleccionStockDTO() {
    }

    public ProductoSeleccionStockDTO(Long id, String nombre, Seccion seccion, CategoriaSimpleDTO categoria, TiendaSimpleDTO tienda) {
        this.id = id;
        this.nombre = nombre;
        this.seccion = seccion;
        this.categoria = categoria;
        this.tienda = tienda;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
