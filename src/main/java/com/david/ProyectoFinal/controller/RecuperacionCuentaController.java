package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.CambiarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarUsuarioRequestDTO;
import com.david.ProyectoFinal.service.RecuperacionCuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/recuperacion")
public class RecuperacionCuentaController {

    private final RecuperacionCuentaService recuperacionCuentaService;

    public RecuperacionCuentaController(RecuperacionCuentaService recuperacionCuentaService) {
        this.recuperacionCuentaService = recuperacionCuentaService;
    }

    @PostMapping("/password")
    public ResponseEntity<String> solicitarRecuperacionPassword(@RequestBody RecuperarPasswordRequestDTO request) {
        try {
            recuperacionCuentaService.solicitarRecuperacionPassword(request);

            return ResponseEntity.ok("Si existe una cuenta con esos datos, recibirás un correo con instrucciones.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(@RequestBody CambiarPasswordRequestDTO request) {
        try {
            recuperacionCuentaService.cambiarPassword(request);

            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/usuario")
    public ResponseEntity<String> solicitarRecuperacionUsuario(@RequestBody RecuperarUsuarioRequestDTO request) {
        try {
            recuperacionCuentaService.solicitarRecuperacionUsuario(request);

            return ResponseEntity.ok("Te hemos enviado un correo con tu nombre de usuario.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}