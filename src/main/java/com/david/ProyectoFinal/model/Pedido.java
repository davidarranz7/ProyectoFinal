package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity/// Convierte la clase en tabla
@Table(name = "pedidos")/// nombre de la Tabla
public class Pedido {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    @ManyToOne/// Muchos pedidos pueden pertenecer a un usuario
    private Usuario usuario;

    private LocalDateTime fechaPedido;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    private MetodoEntrega metodoEntrega;

    @ManyToOne
    @JoinColumn(name = "direccion_envio_id")
    private Direccion direccionEnvio;

    @ManyToOne
    @JoinColumn(name = "establecimiento_recogida_id")
    private Establecimiento establecimientoRecogida;

    @ManyToOne
    @JoinColumn(name = "punto_recogida_id")
    private PuntoRecogida puntoRecogida;

    @Column(unique = true)
    private String tokenConfirmacionEntrega;

    private LocalDateTime fechaExpiracionConfirmacionEntrega;

    private LocalDateTime fechaConfirmacionEntrega;

    private Boolean correoConfirmacionEntregaEnviado = false;

    /// Constructores

    public Pedido() {
    }

    public Pedido(Long id,
                  Usuario usuario,
                  LocalDateTime fechaPedido,
                  BigDecimal total,
                  MetodoPago metodoPago,
                  EstadoPedido estado,
                  MetodoEntrega metodoEntrega,
                  Direccion direccionEnvio,
                  Establecimiento establecimientoRecogida,
                  PuntoRecogida puntoRecogida) {
        this.id = id;
        this.usuario = usuario;
        this.fechaPedido = fechaPedido;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.metodoEntrega = metodoEntrega;
        this.direccionEnvio = direccionEnvio;
        this.establecimientoRecogida = establecimientoRecogida;
        this.puntoRecogida = puntoRecogida;
    }

    /// Getters y Setters

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

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public MetodoEntrega getMetodoEntrega() {
        return metodoEntrega;
    }

    public void setMetodoEntrega(MetodoEntrega metodoEntrega) {
        this.metodoEntrega = metodoEntrega;
    }

    public Direccion getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(Direccion direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public Establecimiento getEstablecimientoRecogida() {
        return establecimientoRecogida;
    }

    public void setEstablecimientoRecogida(Establecimiento establecimientoRecogida) {
        this.establecimientoRecogida = establecimientoRecogida;
    }

    public PuntoRecogida getPuntoRecogida() {
        return puntoRecogida;
    }

    public void setPuntoRecogida(PuntoRecogida puntoRecogida) {
        this.puntoRecogida = puntoRecogida;
    }

    public String getTokenConfirmacionEntrega() {
        return tokenConfirmacionEntrega;
    }

    public void setTokenConfirmacionEntrega(String tokenConfirmacionEntrega) {
        this.tokenConfirmacionEntrega = tokenConfirmacionEntrega;
    }

    public LocalDateTime getFechaExpiracionConfirmacionEntrega() {
        return fechaExpiracionConfirmacionEntrega;
    }

    public void setFechaExpiracionConfirmacionEntrega(LocalDateTime fechaExpiracionConfirmacionEntrega) {
        this.fechaExpiracionConfirmacionEntrega = fechaExpiracionConfirmacionEntrega;
    }

    public LocalDateTime getFechaConfirmacionEntrega() {
        return fechaConfirmacionEntrega;
    }

    public void setFechaConfirmacionEntrega(LocalDateTime fechaConfirmacionEntrega) {
        this.fechaConfirmacionEntrega = fechaConfirmacionEntrega;
    }

    public Boolean getCorreoConfirmacionEntregaEnviado() {
        return correoConfirmacionEntregaEnviado;
    }

    public void setCorreoConfirmacionEntregaEnviado(Boolean correoConfirmacionEntregaEnviado) {
        this.correoConfirmacionEntregaEnviado = correoConfirmacionEntregaEnviado;
    }
}