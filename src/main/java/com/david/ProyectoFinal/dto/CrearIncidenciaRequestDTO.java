package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoIncidencia;

/// Recibe los datos que el usuario escribe en el formulario público de incidencias.
public class CrearIncidenciaRequestDTO {

    private String nombreContacto;
    private String emailContacto;
    private String usuarioRelacionado;
    private Long numeroPedido;
    private TipoIncidencia tipoIncidencia;
    private String asunto;
    private String mensaje;

    public CrearIncidenciaRequestDTO() {
    }

    public CrearIncidenciaRequestDTO(String nombreContacto, String emailContacto, String usuarioRelacionado,
                                     Long numeroPedido, TipoIncidencia tipoIncidencia, String asunto, String mensaje) {
        this.nombreContacto = nombreContacto;
        this.emailContacto = emailContacto;
        this.usuarioRelacionado = usuarioRelacionado;
        this.numeroPedido = numeroPedido;
        this.tipoIncidencia = tipoIncidencia;
        this.asunto = asunto;
        this.mensaje = mensaje;
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

    public String getUsuarioRelacionado() {
        return usuarioRelacionado;
    }

    public void setUsuarioRelacionado(String usuarioRelacionado) {
        this.usuarioRelacionado = usuarioRelacionado;
    }

    public Long getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(Long numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public TipoIncidencia getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(TipoIncidencia tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}