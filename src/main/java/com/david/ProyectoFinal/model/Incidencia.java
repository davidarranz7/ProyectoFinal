package com.david.ProyectoFinal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private String nombreContacto;

    @Column(nullable = false)
    private String emailContacto;

    private String usuarioRelacionado;

    private Long numeroPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIncidencia tipoIncidencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoIncidencia estadoIncidencia = EstadoIncidencia.PENDIENTE;

    @Column(nullable = false)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String mensajeInicial;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltimaActualizacion;

    private LocalDateTime fechaCierre;

    public Incidencia() {
    }

    public Incidencia(Long id, String codigoSeguimiento, String nombreContacto, String emailContacto,
                      String usuarioRelacionado, Long numeroPedido, TipoIncidencia tipoIncidencia,
                      EstadoIncidencia estadoIncidencia, String asunto, String mensajeInicial,
                      LocalDateTime fechaCreacion, LocalDateTime fechaUltimaActualizacion,
                      LocalDateTime fechaCierre) {
        this.id = id;
        this.codigoSeguimiento = codigoSeguimiento;
        this.nombreContacto = nombreContacto;
        this.emailContacto = emailContacto;
        this.usuarioRelacionado = usuarioRelacionado;
        this.numeroPedido = numeroPedido;
        this.tipoIncidencia = tipoIncidencia;
        this.estadoIncidencia = estadoIncidencia;
        this.asunto = asunto;
        this.mensajeInicial = mensajeInicial;
        this.fechaCreacion = fechaCreacion;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
        this.fechaCierre = fechaCierre;
    }

    @PrePersist
    public void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();

        if (this.estadoIncidencia == null) {
            this.estadoIncidencia = EstadoIncidencia.PENDIENTE;
        }
    }

    @PreUpdate
    public void alActualizar() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCodigoSeguimiento() {
        return codigoSeguimiento;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public String getUsuarioRelacionado() {
        return usuarioRelacionado;
    }

    public Long getNumeroPedido() {
        return numeroPedido;
    }

    public TipoIncidencia getTipoIncidencia() {
        return tipoIncidencia;
    }

    public EstadoIncidencia getEstadoIncidencia() {
        return estadoIncidencia;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensajeInicial() {
        return mensajeInicial;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodigoSeguimiento(String codigoSeguimiento) {
        this.codigoSeguimiento = codigoSeguimiento;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public void setUsuarioRelacionado(String usuarioRelacionado) {
        this.usuarioRelacionado = usuarioRelacionado;
    }

    public void setNumeroPedido(Long numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public void setTipoIncidencia(TipoIncidencia tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public void setEstadoIncidencia(EstadoIncidencia estadoIncidencia) {
        this.estadoIncidencia = estadoIncidencia;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public void setMensajeInicial(String mensajeInicial) {
        this.mensajeInicial = mensajeInicial;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}