package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.OrigenMensajeIncidencia;
import com.david.ProyectoFinal.model.RemitenteMensajeIncidencia;

import java.time.LocalDateTime;

/// Devuelve al admin cada mensaje de una incidencia: quién lo escribió, desde dónde llegó, contenido y fecha.
public class MensajeIncidenciaResponseDTO {

    private Long id;
    private Long incidenciaId;
    private RemitenteMensajeIncidencia remitente;
    private OrigenMensajeIncidencia origen;
    private String emailRemitente;
    private String contenido;
    private LocalDateTime fechaMensaje;

    public MensajeIncidenciaResponseDTO() {
    }

    public MensajeIncidenciaResponseDTO(Long id, Long incidenciaId, RemitenteMensajeIncidencia remitente,
                                        OrigenMensajeIncidencia origen, String emailRemitente,
                                        String contenido, LocalDateTime fechaMensaje) {
        this.id = id;
        this.incidenciaId = incidenciaId;
        this.remitente = remitente;
        this.origen = origen;
        this.emailRemitente = emailRemitente;
        this.contenido = contenido;
        this.fechaMensaje = fechaMensaje;
    }

    public Long getId() {
        return id;
    }

    public Long getIncidenciaId() {
        return incidenciaId;
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

    public void setIncidenciaId(Long incidenciaId) {
        this.incidenciaId = incidenciaId;
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