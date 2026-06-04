package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Talla;

import java.util.List;

public class ProductoTallaStockMasivoDTO {

    private List<Long> productoIds;
    private List<Talla> tallas;
    private Integer stock;

    public ProductoTallaStockMasivoDTO() {
    }

    public ProductoTallaStockMasivoDTO(List<Long> productoIds, List<Talla> tallas, Integer stock) {
        this.productoIds = productoIds;
        this.tallas = tallas;
        this.stock = stock;
    }

    public List<Long> getProductoIds() {
        return productoIds;
    }

    public void setProductoIds(List<Long> productoIds) {
        this.productoIds = productoIds;
    }

    public List<Talla> getTallas() {
        return tallas;
    }

    public void setTallas(List<Talla> tallas) {
        this.tallas = tallas;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
