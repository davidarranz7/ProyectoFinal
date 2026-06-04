package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "correos_pendientes")
public class CorreoPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoCorreoPendiente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoCorreoPendiente estado;

    @Column(nullable = false, length = 180)
    private String destinatario;

    @Column(nullable = false, length = 220)
    private String asunto;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String contenido;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imagenInlineBase64;

    @Column(length = 120)
    private String contentId;

    @Column(length = 180)
    private String nombreArchivo;

    @Column(length = 80)
    private String mimeType;

    @Column(nullable = false)
    private Integer intentos;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String ultimoError;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltimoIntento;

    private LocalDateTime fechaEnviado;

    public CorreoPendiente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoCorreoPendiente getTipo() {
        return tipo;
    }

    public void setTipo(TipoCorreoPendiente tipo) {
        this.tipo = tipo;
    }

    public EstadoCorreoPendiente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCorreoPendiente estado) {
        this.estado = estado;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getImagenInlineBase64() {
        return imagenInlineBase64;
    }

    public void setImagenInlineBase64(String imagenInlineBase64) {
        this.imagenInlineBase64 = imagenInlineBase64;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimoIntento() {
        return fechaUltimoIntento;
    }

    public void setFechaUltimoIntento(LocalDateTime fechaUltimoIntento) {
        this.fechaUltimoIntento = fechaUltimoIntento;
    }

    public LocalDateTime getFechaEnviado() {
        return fechaEnviado;
    }

    public void setFechaEnviado(LocalDateTime fechaEnviado) {
        this.fechaEnviado = fechaEnviado;
    }
}
