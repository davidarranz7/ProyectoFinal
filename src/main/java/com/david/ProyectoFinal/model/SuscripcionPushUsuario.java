package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suscripciones_push_usuario")
public class SuscripcionPushUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String endpoint;

    @Column(nullable = false, length = 255)
    private String p256dh;

    @Column(nullable = false, length = 255)
    private String auth;

    private Long expirationTime;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaUltimoEnvioExitoso;

    private LocalDateTime fechaUltimoError;

    @Column(length = 500)
    private String ultimoError;

    public SuscripcionPushUsuario() {
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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getP256dh() {
        return p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaUltimoEnvioExitoso() {
        return fechaUltimoEnvioExitoso;
    }

    public void setFechaUltimoEnvioExitoso(LocalDateTime fechaUltimoEnvioExitoso) {
        this.fechaUltimoEnvioExitoso = fechaUltimoEnvioExitoso;
    }

    public LocalDateTime getFechaUltimoError() {
        return fechaUltimoError;
    }

    public void setFechaUltimoError(LocalDateTime fechaUltimoError) {
        this.fechaUltimoError = fechaUltimoError;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }
}
