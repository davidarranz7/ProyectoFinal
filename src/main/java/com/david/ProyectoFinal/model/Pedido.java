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

    /// Constructores

    public Pedido() {
    }

    public Pedido(Long id, Usuario usuario, LocalDateTime fechaPedido, BigDecimal total, MetodoPago metodoPago, EstadoPedido estado) {
        this.id = id;
        this.usuario = usuario;
        this.fechaPedido = fechaPedido;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
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
}
