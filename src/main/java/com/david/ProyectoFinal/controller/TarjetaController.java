package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.GuardarTarjetaDTO;
import com.david.ProyectoFinal.dto.TarjetaDTO;
import com.david.ProyectoFinal.model.Tarjeta;
import com.david.ProyectoFinal.service.TarjetaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    private void comprobarAccesoUsuario(Long usuarioId, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a las tarjetas de otro usuario");
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TarjetaDTO>> obtenerTarjetasPorUsuario(@PathVariable Long usuarioId,
                                                                      HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return ResponseEntity.ok(tarjetaService.obtenerTarjetasPorUsuario(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<TarjetaDTO> guardarTarjeta(
            @PathVariable Long usuarioId,
            @RequestBody GuardarTarjetaDTO dto,
            HttpSession session) {

        comprobarAccesoUsuario(usuarioId, session);
        return ResponseEntity.ok(tarjetaService.guardarTarjeta(usuarioId, dto));
    }

    @DeleteMapping("/{tarjetaId}")
    public ResponseEntity<String> eliminarTarjeta(@PathVariable Long tarjetaId,
                                                  HttpSession session) {

        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        Tarjeta tarjeta = tarjetaService.obtenerPorId(tarjetaId);

        if (!tarjeta.getUsuario().getId().equals(usuarioIdSesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar una tarjeta de otro usuario");
        }

        tarjetaService.eliminarTarjeta(tarjetaId);
        return ResponseEntity.ok("Tarjeta eliminada correctamente");
    }
}