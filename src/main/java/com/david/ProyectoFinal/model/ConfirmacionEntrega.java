package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmaciones_entrega")
public class ConfirmacionEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// cada confirmación pertenece a un pedido
    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    /// token único que se meterá dentro del QR
    @Column(nullable = false, unique = true)
    private String token;

    /// fecha en la que se crea la confirmación
    private LocalDateTime fechaCreacion;

    /// fecha hasta la que el QR será válido
    private LocalDateTime fechaExpiracion;

    /// indica si el QR ya fue usado
    private Boolean usado;

    /// fecha en la que realmente se usó
    private LocalDateTime fechaUso;

    /// permite invalidar el QR sin borrarlo
    private Boolean activo;

    public ConfirmacionEntrega() {
    }

    public ConfirmacionEntrega(Long id,
                               Pedido pedido,
                               String token,
                               LocalDateTime fechaCreacion,
                               LocalDateTime fechaExpiracion,
                               Boolean usado,
                               LocalDateTime fechaUso,
                               Boolean activo) {
        this.id = id;
        this.pedido = pedido;
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

    public Pedido getPedido() {
        return pedido;
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

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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