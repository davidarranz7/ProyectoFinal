package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Talla;

public class ProductoTallaStockResponseDTO {

    private Talla talla;
    private Integer stock;

    public ProductoTallaStockResponseDTO() {
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
