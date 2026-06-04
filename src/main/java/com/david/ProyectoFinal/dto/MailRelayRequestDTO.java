package com.david.ProyectoFinal.dto;

public class MailRelayRequestDTO {

    private String tipo;
    private String destinatario;
    private String asunto;
    private String contenido;
    private String imagenInlineBase64;
    private String contentId;
    private String nombreArchivo;
    private String mimeType;

    public MailRelayRequestDTO() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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
}
