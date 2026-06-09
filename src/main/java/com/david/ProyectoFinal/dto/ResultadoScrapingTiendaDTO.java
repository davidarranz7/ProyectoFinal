package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoCambioPrecio;

public class ResultadoScrapingTiendaDTO {

    private String tienda;
    private int productosEncontrados;
    private int productosGuardados;
    private int productosNuevos;
    private int productosActualizados;
    private int productosDesactivados;
    private int productosCambioPrecio;
    private int productosBajadaPrecio;
    private int productosSubidaPrecio;
    private int productosRebajaMayor;
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

    public int getProductosDesactivados() {
        return productosDesactivados;
    }

    public void setProductosDesactivados(int productosDesactivados) {
        this.productosDesactivados = productosDesactivados;
    }

    public int getProductosSinImagen() {
        return productosSinImagen;
    }

    public int getProductosCambioPrecio() {
        return productosCambioPrecio;
    }

    public void setProductosCambioPrecio(int productosCambioPrecio) {
        this.productosCambioPrecio = productosCambioPrecio;
    }

    public int getProductosBajadaPrecio() {
        return productosBajadaPrecio;
    }

    public void setProductosBajadaPrecio(int productosBajadaPrecio) {
        this.productosBajadaPrecio = productosBajadaPrecio;
    }

    public int getProductosSubidaPrecio() {
        return productosSubidaPrecio;
    }

    public void setProductosSubidaPrecio(int productosSubidaPrecio) {
        this.productosSubidaPrecio = productosSubidaPrecio;
    }

    public int getProductosRebajaMayor() {
        return productosRebajaMayor;
    }

    public void setProductosRebajaMayor(int productosRebajaMayor) {
        this.productosRebajaMayor = productosRebajaMayor;
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

    public void sumarProductoDesactivado() {
        this.productosDesactivados++;
    }

    public void registrarCambioPrecio(CambioPrecioProductoDTO cambioPrecio) {
        if (cambioPrecio == null) {
            return;
        }

        this.productosCambioPrecio++;

        if (cambioPrecio.getTipoCambio() == TipoCambioPrecio.BAJADA) {
            this.productosBajadaPrecio++;
        } else if (cambioPrecio.getTipoCambio() == TipoCambioPrecio.SUBIDA) {
            this.productosSubidaPrecio++;
        }

        if (Boolean.TRUE.equals(cambioPrecio.getRebajaMayor())) {
            this.productosRebajaMayor++;
        }
    }

    public void sumarProductoSinImagen() {
        this.productosSinImagen++;
    }

    public void sumarProductoSinPrecio() {
        this.productosSinPrecio++;
    }
}
