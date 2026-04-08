package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoTarjeta;

public class TarjetaDTO {

    private Long id;
    private String titular;
    private String numeroEnmascarado;
    private String fechaExpiracion;
    private TipoTarjeta tipo;

    public TarjetaDTO() {
    }

    public TarjetaDTO(Long id, String titular, String numeroEnmascarado, String fechaExpiracion, TipoTarjeta tipo) {
        this.id = id;
        this.titular = titular;
        this.numeroEnmascarado = numeroEnmascarado;
        this.fechaExpiracion = fechaExpiracion;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getNumeroEnmascarado() {
        return numeroEnmascarado;
    }

    public void setNumeroEnmascarado(String numeroEnmascarado) {
        this.numeroEnmascarado = numeroEnmascarado;
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