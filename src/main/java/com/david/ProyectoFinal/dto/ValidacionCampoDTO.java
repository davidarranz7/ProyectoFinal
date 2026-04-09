package com.david.ProyectoFinal.dto;

public class ValidacionCampoDTO {

    private boolean disponible;
    private String mensaje;

    public ValidacionCampoDTO() {
    }

    public ValidacionCampoDTO(boolean disponible, String mensaje) {
        this.disponible = disponible;
        this.mensaje = mensaje;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}