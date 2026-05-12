package com.david.ProyectoFinal.dto;

public class ResultadoScrapingTiendaDTO {

    private String tienda;
    private int productosEncontrados;
    private int productosGuardados;
    private int productosNuevos;
    private int productosActualizados;
    private int productosSinImagen;
    private int productosSinPrecio;

    public ResultadoScrapingTiendaDTO() {
    }

    public ResultadoScrapingTiendaDTO(String tienda) {
        this.tienda = tienda;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public int getProductosEncontrados() {
        return productosEncontrados;
    }

    public void setProductosEncontrados(int productosEncontrados) {
        this.productosEncontrados = productosEncontrados;
    }

    public int getProductosGuardados() {
        return productosGuardados;
    }

    public void setProductosGuardados(int productosGuardados) {
        this.productosGuardados = productosGuardados;
    }

    public int getProductosNuevos() {
        return productosNuevos;
    }

    public void setProductosNuevos(int productosNuevos) {
        this.productosNuevos = productosNuevos;
    }

    public int getProductosActualizados() {
        return productosActualizados;
    }

    public void setProductosActualizados(int productosActualizados) {
        this.productosActualizados = productosActualizados;
    }

    public int getProductosSinImagen() {
        return productosSinImagen;
    }

    public void setProductosSinImagen(int productosSinImagen) {
        this.productosSinImagen = productosSinImagen;
    }

    public int getProductosSinPrecio() {
        return productosSinPrecio;
    }

    public void setProductosSinPrecio(int productosSinPrecio) {
        this.productosSinPrecio = productosSinPrecio;
    }

    public void sumarProductoEncontrado() {
        this.productosEncontrados++;
    }

    public void sumarProductoGuardado() {
        this.productosGuardados++;
    }

    public void sumarProductoNuevo() {
        this.productosNuevos++;
    }

    public void sumarProductoActualizado() {
        this.productosActualizados++;
    }

    public void sumarProductoSinImagen() {
        this.productosSinImagen++;
    }

    public void sumarProductoSinPrecio() {
        this.productosSinPrecio++;
    }
}