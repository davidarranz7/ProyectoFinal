package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoCambioPrecio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CambioPrecioProductoDTO {

    private Long productoId;
    private String nombreProducto;
    private String tienda;
    private String urlProducto;
    private BigDecimal precioAnterior;
    private BigDecimal precioNuevo;
    private BigDecimal precioOriginalAnterior;
    private BigDecimal precioOriginalNuevo;
    private Integer porcentajeDescuentoAnterior;
    private Integer porcentajeDescuentoNuevo;
    private BigDecimal porcentajeVariacionPrecio;
    private Boolean rebajaMayor = false;
    private TipoCambioPrecio tipoCambio;
    private LocalDateTime fechaCambio;

    public CambioPrecioProductoDTO() {
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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
}
