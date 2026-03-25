package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.repository.CarritoRepository;
import com.david.ProyectoFinal.repository.ItemCarritoRepository;
import com.david.ProyectoFinal.repository.PedidoRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;

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

    /// Constructor para inyectar las dependencias
    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, CarritoService carritoService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.carritoService = carritoService;
    }

    /// Método para crear un pedido a partir del carrito de un usuario


}
