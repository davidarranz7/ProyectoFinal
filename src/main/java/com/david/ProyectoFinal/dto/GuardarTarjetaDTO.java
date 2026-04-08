package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoTarjeta;

public class GuardarTarjetaDTO {

    private String titular;
    private String numeroTarjeta;
    private String fechaExpiracion;
    private TipoTarjeta tipo;

    public GuardarTarjetaDTO() {
    }

    public GuardarTarjetaDTO(String titular, String numeroTarjeta, String fechaExpiracion, TipoTarjeta tipo) {
        this.titular = titular;
        this.numeroTarjeta = numeroTarjeta;
        this.fechaExpiracion = fechaExpiracion;
        this.tipo = tipo;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public TipoTarjeta getTipo() {
        return tipo;
    }

    public void setTipo(TipoTarjeta tipo) {
        this.tipo = tipo;
    }
}