package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.NotificacionUsuarioDTO;
import com.david.ProyectoFinal.service.NotificacionUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionUsuarioController {

    private final NotificacionUsuarioService notificacionUsuarioService;

    public NotificacionUsuarioController(NotificacionUsuarioService notificacionUsuarioService) {
        this.notificacionUsuarioService = notificacionUsuarioService;
    }

    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionUsuarioDTO>> obtenerMisNotificaciones(
            @RequestParam(defaultValue = "30") int limit,
            HttpSession session
    ) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        return ResponseEntity.ok(notificacionUsuarioService.obtenerNotificacionesUsuario(usuarioId, limit));
    }

    @GetMapping("/mias/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        long total = notificacionUsuarioService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("total", total));
    }

    @PostMapping("/{notificacionId}/leer")
    public ResponseEntity<NotificacionUsuarioDTO> marcarComoLeida(@PathVariable Long notificacionId,
                                                                  HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        return ResponseEntity.ok(notificacionUsuarioService.marcarComoLeida(usuarioId, notificacionId));
    }

    @PostMapping("/mias/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLeidas(HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        int total = notificacionUsuarioService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("total", total));
    }

    private Long obtenerUsuarioIdSesion(HttpSession session) {
        Long usuarioId = session == null ? null : (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesion iniciada");
        }

        return usuarioId;
    }
}
