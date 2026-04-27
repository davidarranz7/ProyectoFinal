package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.EstadoIncidencia;
import com.david.ProyectoFinal.service.IncidenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/incidencias")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIncidenciaController {

    private final IncidenciaService incidenciaService;

    public AdminIncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerIncidencias(@RequestParam(required = false) String estado) {
        try {
            if (estado == null || estado.isBlank() || estado.equalsIgnoreCase("TODOS")) {
                return ResponseEntity.ok(incidenciaService.obtenerTodasLasIncidencias());
            }

            EstadoIncidencia estadoIncidencia = EstadoIncidencia.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(incidenciaService.obtenerIncidenciasPorEstado(estadoIncidencia));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El estado de incidencia no es válido");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerIncidenciaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(incidenciaService.obtenerIncidenciaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<?> obtenerMensajesDeIncidencia(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(incidenciaService.obtenerMensajesDeIncidencia(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<?> cambiarEstadoIncidencia(@PathVariable Long id,
                                                     @PathVariable String nuevoEstado) {
        try {
            EstadoIncidencia estadoIncidencia = EstadoIncidencia.valueOf(nuevoEstado.toUpperCase());
            return ResponseEntity.ok(incidenciaService.cambiarEstadoIncidencia(id, estadoIncidencia));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El nuevo estado de incidencia no es válido");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}