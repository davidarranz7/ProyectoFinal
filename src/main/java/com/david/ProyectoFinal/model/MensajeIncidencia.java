package com.david.ProyectoFinal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_incidencia")
public class MensajeIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RemitenteMensajeIncidencia remitente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenMensajeIncidencia origen;

    @Column(nullable = false)
    private String emailRemitente;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    private LocalDateTime fechaMensaje;

    public MensajeIncidencia() {
    }

    public MensajeIncidencia(Long id, Incidencia incidencia, RemitenteMensajeIncidencia remitente,
                             OrigenMensajeIncidencia origen, String emailRemitente, String contenido,
                             LocalDateTime fechaMensaje) {
        this.id = id;
        this.incidencia = incidencia;
        this.remitente = remitente;
        this.origen = origen;
        this.emailRemitente = emailRemitente;
        this.contenido = contenido;
        this.fechaMensaje = fechaMensaje;
    }

    @PrePersist
    public void alCrear() {
        this.fechaMensaje = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Incidencia getIncidencia() {
        return incidencia;
    }

    public RemitenteMensajeIncidencia getRemitente() {
        return remitente;
    }

    public OrigenMensajeIncidencia getOrigen() {
        return origen;
    }

    public String getEmailRemitente() {
        return emailRemitente;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechaMensaje() {
        return fechaMensaje;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIncidencia(Incidencia incidencia) {
        this.incidencia = incidencia;
    }

    public void setRemitente(RemitenteMensajeIncidencia remitente) {
        this.remitente = remitente;
    }

    public void setOrigen(OrigenMensajeIncidencia origen) {
        this.origen = origen;
    }

    public void setEmailRemitente(String emailRemitente) {
        this.emailRemitente = emailRemitente;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setFechaMensaje(LocalDateTime fechaMensaje) {
        this.fechaMensaje = fechaMensaje;
    }
}