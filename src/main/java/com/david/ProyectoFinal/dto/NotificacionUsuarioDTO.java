package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoNotificacionUsuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NotificacionUsuarioDTO {

    private Long id;
    private Long productoId;
    private TipoNotificacionUsuario tipo;
    private String titulo;
    private String mensaje;
    private String urlDestino;
    private String urlProductoOriginal;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLeida;
    private BigDecimal precioAnterior;
    private BigDecimal precioNuevo;
    private Integer porcentajeDescuentoNuevo;
    private Boolean rebajaMayor;

    public NotificacionUsuarioDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public TipoNotificacionUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacionUsuario tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getUrlDestino() {
        return urlDestino;
    }

    public void setUrlDestino(String urlDestino) {
        this.urlDestino = urlDestino;
    }

    public String getUrlProductoOriginal() {
        return urlProductoOriginal;
    }

    public void setUrlProductoOriginal(String urlProductoOriginal) {
        this.urlProductoOriginal = urlProductoOriginal;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaLeida() {
        return fechaLeida;
    }

    public void setFechaLeida(LocalDateTime fechaLeida) {
        this.fechaLeida = fechaLeida;
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

    public Integer getPorcentajeDescuentoNuevo() {
        return porcentajeDescuentoNuevo;
    }

    public void setPorcentajeDescuentoNuevo(Integer porcentajeDescuentoNuevo) {
        this.porcentajeDescuentoNuevo = porcentajeDescuentoNuevo;
    }

    public Boolean getRebajaMayor() {
        return rebajaMayor;
    }

    public void setRebajaMayor(Boolean rebajaMayor) {
        this.rebajaMayor = rebajaMayor;
    }
}
