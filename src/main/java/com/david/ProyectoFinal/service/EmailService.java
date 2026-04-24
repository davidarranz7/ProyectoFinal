package com.david.ProyectoFinal.service;

public interface EmailService {

    void enviarCorreoSimple(String destinatario, String asunto, String contenido);

    void enviarCorreoHtml(String destinatario, String asunto, String contenidoHtml);
}