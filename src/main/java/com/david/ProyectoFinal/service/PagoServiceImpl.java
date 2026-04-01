package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.dto.PagoResponseDTO;
import com.david.ProyectoFinal.dto.PagoRequestDTO;
import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository, UsuarioRepository usuarioRepository, CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public PagoResponseDTO procesarPago(PagoRequestDTO dto) {

        PagoResponseDTO response = new PagoResponseDTO();

        Long usuarioId = dto.getUsuarioId();
        MetodoPago metodoPago = dto.getMetodoPago();

        if (metodoPago == null) {
            throw new RuntimeException("El método de pago es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(carrito.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        BigDecimal total = items.stream()
                .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // VALIDACIONES SEGÚN MÉTODO
        if (metodoPago == MetodoPago.TARJETA) {
            String numeroTarjeta = dto.getNumeroTarjeta();
            String nombreTitular = dto.getNombreTitular();
            String fechaExpiracion = dto.getFechaExpiracion();
            String cvv = dto.getCvv();

            if (numeroTarjeta == null || numeroTarjeta.isBlank()) {
                throw new RuntimeException("El número de tarjeta es obligatorio");
            }

            if (nombreTitular == null || nombreTitular.isBlank()) {
                throw new RuntimeException("El nombre del titular es obligatorio");
            }

            if (fechaExpiracion == null || fechaExpiracion.isBlank()) {
                throw new RuntimeException("La fecha de expiración es obligatoria");
            }

            if (cvv == null || cvv.isBlank()) {
                throw new RuntimeException("El CVV es obligatorio");
            }

            if (numeroTarjeta.length() < 12) {
                throw new RuntimeException("Número de tarjeta inválido");
            }

            boolean aprobado = !numeroTarjeta.endsWith("0");

            if (!aprobado) {
                response.setEstado(EstadoPago.RECHAZADO);
                response.setMensaje("Pago rechazado (tarjeta inválida)");
                return response;
            }
        }

        if (metodoPago == MetodoPago.PAYPAL) {
            String emailPaypal = dto.getEmailPaypal();

            if (emailPaypal == null || emailPaypal.isBlank()) {
                throw new RuntimeException("El email de PayPal es obligatorio");
            }

            if (!emailPaypal.contains("@")) {
                throw new RuntimeException("El email de PayPal no es válido");
            }
        }

        if (metodoPago == MetodoPago.CONTRA_REEMBOLSO) {
            Double importeEntrega = dto.getImporteEntrega();

            if (importeEntrega == null) {
                throw new RuntimeException("Debes indicar con cuánto vas a pagar");
            }

            if (importeEntrega < total.doubleValue()) {
                throw new RuntimeException("El importe indicado es insuficiente para el pago contra reembolso");
            }
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setTotal(total);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(EstadoPedido.CONFIRMADO);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        List<ItemPedido> itemsPedido = items.stream().map(itemCarrito -> {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedidoGuardado);
            itemPedido.setProducto(itemCarrito.getProducto());
            itemPedido.setCantidad(itemCarrito.getCantidad());
            itemPedido.setPrecioUnitario(itemCarrito.getProducto().getPrecio());
            return itemPedido;
        }).toList();

        itemPedidoRepository.saveAll(itemsPedido);
        itemCarritoRepository.deleteAll(items);

        Pago pago = new Pago();
        pago.setUsuario(usuario);
        pago.setImporte(total);
        pago.setMetodoPago(metodoPago);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia("PAY-" + System.currentTimeMillis());

        if (metodoPago == MetodoPago.TARJETA) {
            pago.setMensaje("Pago con tarjeta realizado correctamente");
        } else if (metodoPago == MetodoPago.PAYPAL) {
            pago.setMensaje("Pago con PayPal realizado correctamente");
        } else if (metodoPago == MetodoPago.CONTRA_REEMBOLSO) {
            double cambio = dto.getImporteEntrega() - total.doubleValue();
            pago.setMensaje("Pedido confirmado. Cambio a devolver: " + String.format("%.2f", cambio) + " €");
        }

        Pago pagoGuardado = pagoRepository.save(pago);

        response.setPagoId(pagoGuardado.getId());
        response.setEstado(pagoGuardado.getEstado());
        response.setReferencia(pagoGuardado.getReferencia());
        response.setMensaje(pagoGuardado.getMensaje());
        response.setPedidoId(pedidoGuardado.getId());

        return response;
    }
}
