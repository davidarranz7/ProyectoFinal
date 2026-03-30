package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    /// Implementamos las dependecias

    /// Para guardar y consultar pedidos.
    private final PedidoRepository pedidoRepository;
    /// para buscar el usuario que compara
    private final UsuarioRepository usuarioRepository;
    /// para localizar su carrito
    private final CarritoRepository carritoRepository;
    /// para acceder a los productos que hay dentro
    private final ItemCarritoRepository itemCarritoRepository;
    /// para calcular y vaciar el carrtio
    private final CarritoService carritoService;
    /// para acceder a los items de un pedido
    private final ItemPedidoRepository itemPedidoRepository;


    /// Constructor para inyectar las dependencias

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, CarritoService carritoService, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.carritoService = carritoService;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /// Método para crear un pedido a partir del carrito de un usuario

    public Pedido crearPedido(Long usuarioId, MetodoPago metodoPago){

        /// buscar en la base de datos al usuario que quiere comprar
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);

        /// si el usuario no existe, corta el proceso
        if (usuarioOptional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        /// buscar si ese usuario tiene carrito
        Optional<Carrito> carritoOptional = carritoRepository.findByUsuarioId(usuarioId);

        /// si no tiene carrito, no puede comprar nada
        if (carritoOptional.isEmpty()) {
            throw new RuntimeException("Carrito no encontrado para el usuario");
        }

        /// obtener todos los productos que tiene ese carrito
        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(carritoOptional.get().getId());

        /// si el carrito está vacío, no se crea pedido
        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        /// Se crea el objeto del pedido
        Pedido pedido = new Pedido();

        pedido.setUsuario(usuarioOptional.get());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setTotal(carritoService.calcularTotal(usuarioId));
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(EstadoPedido.CONFIRMADO);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        for (ItemCarrito itemCarrito : items) {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedidoGuardado);
            itemPedido.setProducto(itemCarrito.getProducto());
            itemPedido.setCantidad(itemCarrito.getCantidad());
            itemPedido.setPrecioUnitario(itemCarrito.getProducto().getPrecio());

            itemPedidoRepository.save(itemPedido);
        }

        carritoService.vaciarCarrito(usuarioId);

        return pedidoGuardado;

    }

    public List<Pedido> obtenerPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public Pedido cancelarPedido(Long pedidoId) {

        /// buscar el pedido por su id
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);

        /// si no existe, corta el proceso
        if (pedidoOptional.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Pedido pedido = pedidoOptional.get();

        if (pedido.getEstado() == EstadoPedido.ENVIADO ||
                pedido.getEstado() == EstadoPedido.ENTREGADO) {
            return null;
        }

        pedido.setEstado(EstadoPedido.CANCELADO);

        return pedidoRepository.save(pedido);
    }

    /// método para obtener los pedidos de un usuario filtrados por estado
    public List<Pedido> obtenerPedidosPorUsuarioYEstado(Long usuarioId, EstadoPedido estado) {
        return pedidoRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }

    /// método para obtener los items de un pedido específico
    public List<ItemPedido> obtenerItemsDePedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }


}
