package com.david.ProyectoFinal.dto;

import java.time.LocalDateTime;

public class ScrapingEjecucionAdminDTO {

    private Long id;
    private String tipo;
    private String nombreProceso;
    private String origen;
    private String estado;
    private Boolean relayHabilitado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long duracionMs;
    private Integer totalProductosEncontrados;
    private Integer totalProductosGuardados;
    private Integer totalProductosNuevos;
    private Integer totalProductosActualizados;
    private Integer totalProductosDesactivados;
    private Integer totalProductosCambioPrecio;
    private Integer totalProductosBajadaPrecio;
    private Integer totalProductosSubidaPrecio;
    private String mensajeEstado;
    private String detalleError;

    public ScrapingEjecucionAdminDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setNombreProceso(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getRelayHabilitado() {
        return relayHabilitado;
    }

    public void setRelayHabilitado(Boolean relayHabilitado) {
        this.relayHabilitado = relayHabilitado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Long getDuracionMs() {
        return duracionMs;
    }

    public void setDuracionMs(Long duracionMs) {
        this.duracionMs = duracionMs;
    }

    public Integer getTotalProductosEncontrados() {
        return totalProductosEncontrados;
    }

    public void setTotalProductosEncontrados(Integer totalProductosEncontrados) {
        this.totalProductosEncontrados = totalProductosEncontrados;
    }

    public Integer getTotalProductosGuardados() {
        return totalProductosGuardados;
    }

    public void setTotalProductosGuardados(Integer totalProductosGuardados) {
        this.totalProductosGuardados = totalProductosGuardados;
    }

    public Integer getTotalProductosNuevos() {
        return totalProductosNuevos;
    }

    public void setTotalProductosNuevos(Integer totalProductosNuevos) {
        this.totalProductosNuevos = totalProductosNuevos;
    }

    public Integer getTotalProductosActualizados() {
        return totalProductosActualizados;
    }

    public void setTotalProductosActualizados(Integer totalProductosActualizados) {
        this.totalProductosActualizados = totalProductosActualizados;
    }

    public Integer getTotalProductosDesactivados() {
        return totalProductosDesactivados;
    }

    public void setTotalProductosDesactivados(Integer totalProductosDesactivados) {
        this.totalProductosDesactivados = totalProductosDesactivados;
    }

    public Integer getTotalProductosCambioPrecio() {
        return totalProductosCambioPrecio;
    }

    public void setTotalProductosCambioPrecio(Integer totalProductosCambioPrecio) {
        this.totalProductosCambioPrecio = totalProductosCambioPrecio;
    }

    public Integer getTotalProductosBajadaPrecio() {
        return totalProductosBajadaPrecio;
    }

    public void setTotalProductosBajadaPrecio(Integer totalProductosBajadaPrecio) {
        this.totalProductosBajadaPrecio = totalProductosBajadaPrecio;
    }

    public Integer getTotalProductosSubidaPrecio() {
        return totalProductosSubidaPrecio;
    }

    public void setTotalProductosSubidaPrecio(Integer totalProductosSubidaPrecio) {
        this.totalProductosSubidaPrecio = totalProductosSubidaPrecio;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public void setMensajeEstado(String mensajeEstado) {
        this.mensajeEstado = mensajeEstado;
    }

    public String getDetalleError() {
        return detalleError;
    }

    public void setDetalleError(String detalleError) {
        this.detalleError = detalleError;
    }
}
