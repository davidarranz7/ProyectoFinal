package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-email")
    public String probarEmail() {
        emailService.enviarCorreoSimple(
                "gomezramosgisela@gmail.com",
                "Prueba de correo SpotifyPro",
                "Si estás leyendo esto, el envío de correo funciona correctamente."
        );

        return "Correo enviado correctamente";
    }
}