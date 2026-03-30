package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Talla;

public class ProductoTallaStockDTO {

    private Long productoId;/// Para identificar a qué producto se refiere este stock de talla.
    private Talla talla;/// Para identificar la talla específica.
    private Integer stock;/// Para indicar cuántas unidades de esa talla están disponibles.


    public ProductoTallaStockDTO() {
    }

    public ProductoTallaStockDTO(Long productoId, Talla talla, Integer stock) {
        this.productoId = productoId;
        this.talla = talla;
        this.stock = stock;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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
