package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.MetodoPago;
import com.david.ProyectoFinal.model.TipoTarjeta;

/// Esto representa los datos que el usuario envía para pagar.
public class PagoRequestDTO {

    private Long usuarioId;
    private MetodoPago metodoPago;

    /// si usa una tarjeta ya guardada
    private Long tarjetaId;

    /// si quiere guardar una tarjeta nueva
    private Boolean guardarTarjeta;

    /// datos de tarjeta nueva
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaExpiracion;
    private String cvv;
    private TipoTarjeta tipoTarjeta;

    private String emailPaypal;

    private Double importeEntrega;

    public PagoRequestDTO() {
    }

    public PagoRequestDTO(Long usuarioId,
                          MetodoPago metodoPago,
                          Long tarjetaId,
                          Boolean guardarTarjeta,
                          String numeroTarjeta,
                          String nombreTitular,
                          String fechaExpiracion,
                          String cvv,
                          TipoTarjeta tipoTarjeta,
                          String emailPaypal,
                          Double importeEntrega) {
        this.usuarioId = usuarioId;
        this.metodoPago = metodoPago;
        this.tarjetaId = tarjetaId;
        this.guardarTarjeta = guardarTarjeta;
        this.numeroTarjeta = numeroTarjeta;
        this.nombreTitular = nombreTitular;
        this.fechaExpiracion = fechaExpiracion;
        this.cvv = cvv;
        this.tipoTarjeta = tipoTarjeta;
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

    public Long getTarjetaId() {
        return tarjetaId;
    }

    public void setTarjetaId(Long tarjetaId) {
        this.tarjetaId = tarjetaId;
    }

    public Boolean getGuardarTarjeta() {
        return guardarTarjeta;
    }

    public void setGuardarTarjeta(Boolean guardarTarjeta) {
        this.guardarTarjeta = guardarTarjeta;
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

    public TipoTarjeta getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(TipoTarjeta tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
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