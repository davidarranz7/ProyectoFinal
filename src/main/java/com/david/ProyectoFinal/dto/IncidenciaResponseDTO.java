package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.EstadoIncidencia;
import com.david.ProyectoFinal.model.TipoIncidencia;

import java.time.LocalDateTime;

public class IncidenciaResponseDTO {

    private Long id;
    private String codigoSeguimiento;
    private String nombreContacto;
    private String emailContacto;
    private TipoIncidencia tipoIncidencia;
    private EstadoIncidencia estadoIncidencia;
    private String asunto;
    private LocalDateTime fechaCreacion;
    private boolean correoPendiente;
    private String mensajeCorreo;

    public IncidenciaResponseDTO() {
    }

    public IncidenciaResponseDTO(Long id,
                                 String codigoSeguimiento,
                                 String nombreContacto,
                                 String emailContacto,
                                 TipoIncidencia tipoIncidencia,
                                 EstadoIncidencia estadoIncidencia,
                                 String asunto,
                                 LocalDateTime fechaCreacion) {
        this.id = id;
        this.codigoSeguimiento = codigoSeguimiento;
        this.nombreContacto = nombreContacto;
        this.emailContacto = emailContacto;
        this.tipoIncidencia = tipoIncidencia;
        this.estadoIncidencia = estadoIncidencia;
        this.asunto = asunto;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public void setCodigoSeguimiento(String codigoSeguimiento) {
        this.codigoSeguimiento = codigoSeguimiento;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public TipoIncidencia getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(TipoIncidencia tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public EstadoIncidencia getEstadoIncidencia() {
        return estadoIncidencia;
    }

    public void setEstadoIncidencia(EstadoIncidencia estadoIncidencia) {
        this.estadoIncidencia = estadoIncidencia;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isCorreoPendiente() {
        return correoPendiente;
    }

    public void setCorreoPendiente(boolean correoPendiente) {
        this.correoPendiente = correoPendiente;
    }

    public String getMensajeCorreo() {
        return mensajeCorreo;
    }

    public void setMensajeCorreo(String mensajeCorreo) {
        this.mensajeCorreo = mensajeCorreo;
    }
}
