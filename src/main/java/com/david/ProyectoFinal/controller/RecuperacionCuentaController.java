package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.CambiarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.CorreoOperacionResponseDTO;
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
    public ResponseEntity<?> solicitarRecuperacionPassword(@RequestBody RecuperarPasswordRequestDTO request) {
        try {
            CorreoOperacionResponseDTO respuesta = recuperacionCuentaService.solicitarRecuperacionPassword(request);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(@RequestBody CambiarPasswordRequestDTO request) {
        try {
            recuperacionCuentaService.cambiarPassword(request);
            return ResponseEntity.ok("Contrasena actualizada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/usuario")
    public ResponseEntity<?> solicitarRecuperacionUsuario(@RequestBody RecuperarUsuarioRequestDTO request) {
        try {
            CorreoOperacionResponseDTO respuesta = recuperacionCuentaService.solicitarRecuperacionUsuario(request);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
