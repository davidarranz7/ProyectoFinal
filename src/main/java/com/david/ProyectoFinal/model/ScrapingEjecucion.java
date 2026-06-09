package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraping_ejecuciones")
public class ScrapingEjecucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoScrapingPendiente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrigenScrapingEjecucion origen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoScrapingEjecucion estado;

    @Column(nullable = false)
    private Boolean relayHabilitado;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Long duracionMs;

    private Integer totalProductosEncontrados = 0;

    private Integer totalProductosGuardados = 0;

    private Integer totalProductosNuevos = 0;

    private Integer totalProductosActualizados = 0;

    private Integer totalProductosDesactivados = 0;

    private Integer totalProductosCambioPrecio = 0;

    private Integer totalProductosBajadaPrecio = 0;

    private Integer totalProductosSubidaPrecio = 0;

    private Integer totalProductosRebajaMayor = 0;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String mensajeEstado;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String detalleError;

    public ScrapingEjecucion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoScrapingPendiente getTipo() {
        return tipo;
    }

    public void setTipo(TipoScrapingPendiente tipo) {
        this.tipo = tipo;
    }

    public OrigenScrapingEjecucion getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenScrapingEjecucion origen) {
        this.origen = origen;
    }

    public EstadoScrapingEjecucion getEstado() {
        return estado;
    }

    public void setEstado(EstadoScrapingEjecucion estado) {
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

    public Integer getTotalProductosRebajaMayor() {
        return totalProductosRebajaMayor;
    }

    public void setTotalProductosRebajaMayor(Integer totalProductosRebajaMayor) {
        this.totalProductosRebajaMayor = totalProductosRebajaMayor;
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
