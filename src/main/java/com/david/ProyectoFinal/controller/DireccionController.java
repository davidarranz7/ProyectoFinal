package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.ActualizarDireccionDTO;
import com.david.ProyectoFinal.dto.CrearDireccionDTO;
import com.david.ProyectoFinal.dto.DireccionDTO;
import com.david.ProyectoFinal.service.DireccionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    private void comprobarAccesoUsuario(Long usuarioId, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a las direcciones de otro usuario");
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<DireccionDTO>> obtenerDireccionesDeUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(direccionService.obtenerDireccionesDeUsuario(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<DireccionDTO> crearDireccion(@PathVariable Long usuarioId,
                                                       @RequestBody CrearDireccionDTO dto) {
        return ResponseEntity.ok(direccionService.crearDireccion(usuarioId, dto));
    }

    @PutMapping("/usuario/{usuarioId}/{direccionId}")
    public ResponseEntity<DireccionDTO> actualizarDireccion(@PathVariable Long usuarioId,
                                                            @PathVariable Long direccionId,
                                                            @RequestBody ActualizarDireccionDTO dto) {
        return ResponseEntity.ok(direccionService.actualizarDireccion(usuarioId, direccionId, dto));
    }

    @DeleteMapping("/usuario/{usuarioId}/{direccionId}")
    public ResponseEntity<String> eliminarDireccion(@PathVariable Long usuarioId,
                                                    @PathVariable Long direccionId) {
        direccionService.eliminarDireccion(usuarioId, direccionId);
        return ResponseEntity.ok("Dirección eliminada correctamente");
    }

    @PutMapping("/usuario/{usuarioId}/{direccionId}/principal")
    public ResponseEntity<DireccionDTO> marcarComoPrincipal(@PathVariable Long usuarioId,
                                                            @PathVariable Long direccionId) {
        return ResponseEntity.ok(direccionService.marcarComoPrincipal(usuarioId, direccionId));
    }
}