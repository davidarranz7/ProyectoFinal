package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.EstadoPago;

/// Esto es lo que devolverá el backend después de procesar el pago.
public class PagoResponseDTO {

    private Long pagoId;
    private EstadoPago estado;
    private String referencia;
    private String mensaje;
    private Long pedidoId;

    public PagoResponseDTO() {
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
}

