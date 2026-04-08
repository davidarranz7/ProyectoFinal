package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.GuardarTarjetaDTO;
import com.david.ProyectoFinal.dto.TarjetaDTO;
import com.david.ProyectoFinal.service.TarjetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TarjetaDTO>> obtenerTarjetasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(tarjetaService.obtenerTarjetasPorUsuario(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<TarjetaDTO> guardarTarjeta(
            @PathVariable Long usuarioId,
            @RequestBody GuardarTarjetaDTO dto) {

        return ResponseEntity.ok(tarjetaService.guardarTarjeta(usuarioId, dto));
    }

    @DeleteMapping("/{tarjetaId}")
    public ResponseEntity<String> eliminarTarjeta(@PathVariable Long tarjetaId) {
        tarjetaService.eliminarTarjeta(tarjetaId);
        return ResponseEntity.ok("Tarjeta eliminada correctamente");
    }
}