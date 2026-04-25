package com.david.ProyectoFinal.dto;

public class ConfirmacionEntregaValidacionResponseDTO {

    private Long pedidoId;
    private String estadoPedido;
    private String mensaje;
    private Boolean valido;

    public ConfirmacionEntregaValidacionResponseDTO() {
    }

    public ConfirmacionEntregaValidacionResponseDTO(Long pedidoId,
                                                    String estadoPedido,
                                                    String mensaje,
                                                    Boolean valido) {
        this.pedidoId = pedidoId;
        this.estadoPedido = estadoPedido;
        this.mensaje = mensaje;
        this.valido = valido;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Boolean getValido() {
        return valido;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setValido(Boolean valido) {
        this.valido = valido;
    }
}