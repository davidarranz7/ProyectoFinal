package com.david.ProyectoFinal.dto;

import java.util.List;

public class ProductoPageResponseDTO {

    private List<ProductoListadoDTO> productos;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private boolean ultimaPagina;

    public ProductoPageResponseDTO() {
    }

    public ProductoPageResponseDTO(List<ProductoListadoDTO> productos,
                                   int paginaActual,
                                   int totalPaginas,
                                   long totalElementos,
                                   boolean ultimaPagina) {
        this.productos = productos;
        this.paginaActual = paginaActual;
        this.totalPaginas = totalPaginas;
        this.totalElementos = totalElementos;
        this.ultimaPagina = ultimaPagina;
    }

    public List<ProductoListadoDTO> getProductos() {
        return productos;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public boolean isUltimaPagina() {
        return ultimaPagina;
    }

    public void setProductos(List<ProductoListadoDTO> productos) {
        this.productos = productos;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public void setUltimaPagina(boolean ultimaPagina) {
        this.ultimaPagina = ultimaPagina;
    }
}