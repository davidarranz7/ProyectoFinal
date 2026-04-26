package com.david.ProyectoFinal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    public PasswordResetToken() {
    }

    public PasswordResetToken(Long id, String token, Usuario usuario, LocalDateTime fechaExpiracion, boolean usado) {
        this.id = id;
        this.token = token;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}