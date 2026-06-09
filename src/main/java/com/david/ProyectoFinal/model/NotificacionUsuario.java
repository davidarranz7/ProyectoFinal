package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_usuario")
public class NotificacionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacionUsuario tipo;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(length = 300)
    private String urlDestino;

    @Column(columnDefinition = "TEXT")
    private String urlProductoOriginal;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(nullable = false)
    private Boolean emailEnviado = false;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaLeida;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    private Integer porcentajeDescuentoNuevo;

    @Column(nullable = false)
    private Boolean rebajaMayor = false;

    public NotificacionUsuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

    public Boolean getEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(Boolean emailEnviado) {
        this.emailEnviado = emailEnviado;
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
