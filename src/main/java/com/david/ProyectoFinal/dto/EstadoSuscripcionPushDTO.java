package com.david.ProyectoFinal.dto;

public class EstadoSuscripcionPushDTO {

    private Boolean habilitado;
    private Boolean configurado;
    private Boolean suscrito;
    private String clavePublica;
    private String mensaje;

    public EstadoSuscripcionPushDTO() {
    }

    public Boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(Boolean habilitado) {
        this.habilitado = habilitado;
    }

    public Boolean getConfigurado() {
        return configurado;
    }

    public void setConfigurado(Boolean configurado) {
        this.configurado = configurado;
    }

    public Boolean getSuscrito() {
        return suscrito;
    }

    public void setSuscrito(Boolean suscrito) {
        this.suscrito = suscrito;
    }

    public String getClavePublica() {
        return clavePublica;
    }

    public void setClavePublica(String clavePublica) {
        this.clavePublica = clavePublica;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
