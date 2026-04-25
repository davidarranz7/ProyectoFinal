package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.ConfirmacionEntregaResponseDTO;
import com.david.ProyectoFinal.dto.ConfirmacionEntregaValidacionResponseDTO;
import com.david.ProyectoFinal.service.ConfirmacionEntregaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirmaciones-entrega")
public class ConfirmacionEntregaController {

    private final ConfirmacionEntregaService confirmacionEntregaService;

    public ConfirmacionEntregaController(ConfirmacionEntregaService confirmacionEntregaService) {
        this.confirmacionEntregaService = confirmacionEntregaService;
    }

    /// crea manualmente una confirmación para un pedido
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pedido/{pedidoId}")
    public ConfirmacionEntregaResponseDTO crearConfirmacionParaPedido(@PathVariable Long pedidoId) {
        return confirmacionEntregaService.crearConfirmacionParaPedidoDTO(pedidoId);
    }

    /// valida un token sin cerrar todavía el pedido
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/validar")
    public ConfirmacionEntregaResponseDTO validarToken(@RequestParam String token) {
        return confirmacionEntregaService.validarTokenDTO(token);
    }

    /// confirma la entrega usando el token del QR
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/confirmar")
    public ConfirmacionEntregaValidacionResponseDTO confirmarEntregaConToken(@RequestParam String token) {
        return confirmacionEntregaService.confirmarEntregaConTokenDTO(token);
    }

    /// obtiene la confirmación asociada a un pedido
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pedido/{pedidoId}")
    public ConfirmacionEntregaResponseDTO obtenerPorPedido(@PathVariable Long pedidoId) {
        return confirmacionEntregaService.obtenerPorPedidoDTO(pedidoId);
    }
}