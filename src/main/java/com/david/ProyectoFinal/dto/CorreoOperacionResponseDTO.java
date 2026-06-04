package com.david.ProyectoFinal.dto;

public class CorreoOperacionResponseDTO {

    private String mensaje;
    private boolean correoPendiente;

    public CorreoOperacionResponseDTO() {
    }

    public CorreoOperacionResponseDTO(String mensaje, boolean correoPendiente) {
        this.mensaje = mensaje;
        this.correoPendiente = correoPendiente;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isCorreoPendiente() {
        return correoPendiente;
    }

    public void setCorreoPendiente(boolean correoPendiente) {
        this.correoPendiente = correoPendiente;
    }
}
