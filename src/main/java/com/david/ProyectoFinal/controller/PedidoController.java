package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.ItemPedido;
import com.david.ProyectoFinal.model.MetodoPago;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController/// /// indicamos que es un controllador
@RequestMapping("/pedidos")/// ruta base para todas las operaciones relacionadas con el carrito
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    private void comprobarAccesoUsuario(Long usuarioId, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a los pedidos de otro usuario");
        }
    }

    private void comprobarAccesoPedido(Pedido pedido, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!pedido.getUsuario().getId().equals(usuarioIdSesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a un pedido de otro usuario");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long id,
                                                     HttpSession session) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        comprobarAccesoPedido(pedido, session);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/crear")/// ruta para crear un pedido a partir del carrito de un usuario
    public Pedido crearPedido(@RequestParam Long usuarioId,
                              @RequestParam MetodoPago metodoPago,
                              HttpSession session){
        comprobarAccesoUsuario(usuarioId, session);
        return pedidoService.crearPedido(usuarioId, metodoPago);
    }

    @GetMapping("/usuario/{usuarioId}")/// ruta para obtener los pedidos de un usuario
    public List<Pedido> obtenerPedidosPorUsuario(@PathVariable Long usuarioId,
                                                 HttpSession session){
        comprobarAccesoUsuario(usuarioId, session);
        return pedidoService.obtenerPedidosPorUsuario(usuarioId);
    }

    @PutMapping("/cancelar/{pedidoId}")/// ruta para cancelar un pedido
    public Pedido cancelarPedido(@PathVariable Long pedidoId,
                                 HttpSession session){
        Pedido pedido = pedidoService.obtenerPorId(pedidoId);
        comprobarAccesoPedido(pedido, session);
        return pedidoService.cancelarPedido(pedidoId);
    }

    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public List<Pedido> obtenerPedidosPorUsuarioYEstado(@PathVariable Long usuarioId,
                                                        @PathVariable EstadoPedido estado,
                                                        HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return pedidoService.obtenerPedidosPorUsuarioYEstado(usuarioId, estado);
    }

    @GetMapping("/{pedidoId}/items")/// ruta para obtener un pedido por su id
    public List<ItemPedido> obtenerItemsDePedido(@PathVariable Long pedidoId,
                                                 HttpSession session) {
        Pedido pedido = pedidoService.obtenerPorId(pedidoId);
        comprobarAccesoPedido(pedido, session);
        return pedidoService.obtenerItemsDePedido(pedidoId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoService.obtenerTodosLosPedidos();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estado}")
    public List<Pedido> obtenerPedidosPorEstado(@PathVariable EstadoPedido estado) {
        return pedidoService.obtenerPedidosPorEstado(estado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{pedidoId}/estado/{nuevoEstado}")
    public Pedido cambiarEstadoPedido(@PathVariable Long pedidoId,
                                      @PathVariable EstadoPedido nuevoEstado) {
        return pedidoService.cambiarEstadoPedido(pedidoId, nuevoEstado);
    }
}