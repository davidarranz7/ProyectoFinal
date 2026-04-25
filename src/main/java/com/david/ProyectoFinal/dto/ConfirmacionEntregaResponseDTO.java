package com.david.ProyectoFinal.dto;

import java.time.LocalDateTime;

public class ConfirmacionEntregaResponseDTO {

    private Long id;
    private Long pedidoId;
    private String token;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private Boolean usado;
    private LocalDateTime fechaUso;
    private Boolean activo;

    public ConfirmacionEntregaResponseDTO() {
    }

    public ConfirmacionEntregaResponseDTO(Long id,
                                          Long pedidoId,
                                          String token,
                                          LocalDateTime fechaCreacion,
                                          LocalDateTime fechaExpiracion,
                                          Boolean usado,
                                          LocalDateTime fechaUso,
                                          Boolean activo) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.token = token;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
        this.fechaUso = fechaUso;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}