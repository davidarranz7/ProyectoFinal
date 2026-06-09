package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.EstadoSuscripcionPushDTO;
import com.david.ProyectoFinal.dto.SuscripcionPushRequestDTO;
import com.david.ProyectoFinal.service.NotificacionPushService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/push-notificaciones")
public class NotificacionPushController {

    private final NotificacionPushService notificacionPushService;

    public NotificacionPushController(NotificacionPushService notificacionPushService) {
        this.notificacionPushService = notificacionPushService;
    }

    @GetMapping("/estado")
    public ResponseEntity<EstadoSuscripcionPushDTO> obtenerEstado(HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        return ResponseEntity.ok(notificacionPushService.obtenerEstadoUsuario(usuarioId));
    }

    @PostMapping("/suscripcion")
    public ResponseEntity<EstadoSuscripcionPushDTO> guardarSuscripcion(@RequestBody SuscripcionPushRequestDTO request,
                                                                      HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        return ResponseEntity.ok(notificacionPushService.guardarSuscripcion(usuarioId, request));
    }

    @DeleteMapping("/suscripcion")
    public ResponseEntity<EstadoSuscripcionPushDTO> eliminarSuscripcion(@RequestBody(required = false) SuscripcionPushRequestDTO request,
                                                                        HttpSession session) {
        Long usuarioId = obtenerUsuarioIdSesion(session);
        String endpoint = request == null ? null : request.getEndpoint();
        return ResponseEntity.ok(notificacionPushService.eliminarSuscripcion(usuarioId, endpoint));
    }

    private Long obtenerUsuarioIdSesion(HttpSession session) {
        Long usuarioId = session == null ? null : (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesion iniciada");
        }

        return usuarioId;
    }
}
