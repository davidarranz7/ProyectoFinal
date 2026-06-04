package com.david.ProyectoFinal.service;

public interface EmailService {

    void enviarCorreoSimple(String destinatario, String asunto, String contenido);

    void enviarCorreoHtml(String destinatario, String asunto, String contenidoHtml);

    void enviarCorreoHtmlConImagenInline(String destinatario,
                                         String asunto,
                                         String contenidoHtml,
                                         byte[] imagenBytes,
                                         String contentId,
                                         String nombreArchivo);

    EmailDispatchResult enviarCorreoSimpleConResultado(String destinatario, String asunto, String contenido);

    EmailDispatchResult enviarCorreoHtmlConResultado(String destinatario, String asunto, String contenidoHtml);

    EmailDispatchResult enviarCorreoHtmlConImagenInlineConResultado(String destinatario,
                                                                    String asunto,
                                                                    String contenidoHtml,
                                                                    byte[] imagenBytes,
                                                                    String contentId,
                                                                    String nombreArchivo);
}
