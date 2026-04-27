package com.david.ProyectoFinal.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailEntranteIncidenciaScheduler {

    private final EmailEntranteIncidenciaService emailEntranteIncidenciaService;

    public EmailEntranteIncidenciaScheduler(EmailEntranteIncidenciaService emailEntranteIncidenciaService) {
        this.emailEntranteIncidenciaService = emailEntranteIncidenciaService;
    }

    @Scheduled(fixedDelayString = "${app.mail.imap.interval-ms:60000}")
    public void leerCorreosEntrantesAutomaticamente() {
        try {
            int correosProcesados = emailEntranteIncidenciaService.leerCorreosEntrantesDeIncidencias();

            if (correosProcesados > 0) {
                System.out.println("Correos de incidencias procesados automáticamente: " + correosProcesados);
            }

        } catch (Exception e) {
            System.out.println("ERROR EN LECTURA AUTOMÁTICA DE CORREOS DE INCIDENCIAS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}