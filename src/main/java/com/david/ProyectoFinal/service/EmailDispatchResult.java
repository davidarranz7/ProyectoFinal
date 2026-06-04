package com.david.ProyectoFinal.service;

public class EmailDispatchResult {

    private final boolean pendiente;
    private final String mensaje;

    private EmailDispatchResult(boolean pendiente, String mensaje) {
        this.pendiente = pendiente;
        this.mensaje = mensaje;
    }

    public static EmailDispatchResult enviado(String mensaje) {
        return new EmailDispatchResult(false, mensaje);
    }

    public static EmailDispatchResult pendiente(String mensaje) {
        return new EmailDispatchResult(true, mensaje);
    }

    public boolean isPendiente() {
        return pendiente;
    }

    public String getMensaje() {
        return mensaje;
    }
}
