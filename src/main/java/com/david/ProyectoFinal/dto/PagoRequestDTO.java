package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.MetodoPago;

/// Esto representa los datos que el usuario envía para pagar.
public class PagoRequestDTO {

    private Long usuarioId;
    private MetodoPago metodoPago;

    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaExpiracion;
    private String cvv;

    private String emailPaypal;

    private Double importeEntrega;

    public PagoRequestDTO() {
    }

    public PagoRequestDTO(Long usuarioId,
                          MetodoPago metodoPago,
                          String numeroTarjeta,
                          String nombreTitular,
                          String fechaExpiracion,
                          String cvv,
                          String emailPaypal,
                          Double importeEntrega) {
        this.usuarioId = usuarioId;
        this.metodoPago = metodoPago;
        this.numeroTarjeta = numeroTarjeta;
        this.nombreTitular = nombreTitular;
        this.fechaExpiracion = fechaExpiracion;
        this.cvv = cvv;
        this.emailPaypal = emailPaypal;
        this.importeEntrega = importeEntrega;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getEmailPaypal() {
        return emailPaypal;
    }

    public void setEmailPaypal(String emailPaypal) {
        this.emailPaypal = emailPaypal;
    }

    public Double getImporteEntrega() {
        return importeEntrega;
    }

    public void setImporteEntrega(Double importeEntrega) {
        this.importeEntrega = importeEntrega;
    }
}