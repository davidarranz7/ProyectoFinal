package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.CrearIncidenciaRequestDTO;
import com.david.ProyectoFinal.dto.IncidenciaResponseDTO;
import com.david.ProyectoFinal.service.IncidenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @PostMapping
    public ResponseEntity<?> crearIncidencia(@RequestBody CrearIncidenciaRequestDTO request) {
        try {
            IncidenciaResponseDTO incidenciaCreada = incidenciaService.crearIncidencia(request);
            return ResponseEntity.ok(incidenciaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}