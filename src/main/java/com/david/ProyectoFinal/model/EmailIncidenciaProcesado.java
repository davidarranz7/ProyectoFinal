package com.david.ProyectoFinal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emails_incidencia_procesados")
public class EmailIncidenciaProcesado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageIdEmail;

    @ManyToOne
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    private LocalDateTime fechaProcesado;

    public EmailIncidenciaProcesado() {
    }

    public EmailIncidenciaProcesado(Long id, String messageIdEmail, Incidencia incidencia, LocalDateTime fechaProcesado) {
        this.id = id;
        this.messageIdEmail = messageIdEmail;
        this.incidencia = incidencia;
        this.fechaProcesado = fechaProcesado;
    }

    @PrePersist
    public void alCrear() {
        this.fechaProcesado = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getMessageIdEmail() {
        return messageIdEmail;
    }

    public Incidencia getIncidencia() {
        return incidencia;
    }

    public LocalDateTime getFechaProcesado() {
        return fechaProcesado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessageIdEmail(String messageIdEmail) {
        this.messageIdEmail = messageIdEmail;
    }

    public void setIncidencia(Incidencia incidencia) {
        this.incidencia = incidencia;
    }

    public void setFechaProcesado(LocalDateTime fechaProcesado) {
        this.fechaProcesado = fechaProcesado;
    }
}