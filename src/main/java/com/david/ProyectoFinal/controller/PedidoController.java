package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.ItemPedido;
import com.david.ProyectoFinal.model.MetodoPago;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController/// /// indicamos que es un controllador
@RequestMapping("/pedidos")/// ruta base para todas las operaciones relacionadas con el carrito
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping("/crear")/// ruta para crear un pedido a partir del carrito de un usuario
    public Pedido crearpedido(@RequestParam Long usuarioId,
                              @RequestParam MetodoPago metodoPago){
        return pedidoService.crearPedido(usuarioId, metodoPago);
    }

    @GetMapping("/usuario/{usuarioId}")/// ruta para obtener los pedidos de un usuario
    public List<Pedido> obtenerPedidosPorUsuario(@PathVariable Long usuarioId){
        return pedidoService.obtenerPedidosPorUsuario(usuarioId);
    }

    @PutMapping("/cancelar/{pedidoId}")/// ruta para cancelar un pedido
    public Pedido cancelarPedido(@PathVariable Long pedidoId){
        return pedidoService.cancelarPedido(pedidoId);
    }
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public List<Pedido> obtenerPedidosPorUsuarioYEstado(@PathVariable Long usuarioId,
                                                        @PathVariable EstadoPedido estado) {
        return pedidoService.obtenerPedidosPorUsuarioYEstado(usuarioId, estado);
    }

    @GetMapping("/{pedidoId}/items")/// ruta para obtener un pedido por su id
    public List<ItemPedido> obtenerItemsDePedido(@PathVariable Long pedidoId) {
        return pedidoService.obtenerItemsDePedido(pedidoId);
    }
}
