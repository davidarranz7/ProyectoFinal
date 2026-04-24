package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.GuardarTarjetaDTO;
import com.david.ProyectoFinal.dto.PagoResponseDTO;
import com.david.ProyectoFinal.dto.PagoRequestDTO;
import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EmailService emailService;
    private final TarjetaRepository tarjetaRepository;
    private final TarjetaService tarjetaService;

    public PagoServiceImpl(PagoRepository pagoRepository, UsuarioRepository usuarioRepository, CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository, EmailService emailService, TarjetaRepository tarjetaRepository, TarjetaService tarjetaService) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.emailService = emailService;
        this.tarjetaRepository = tarjetaRepository;
        this.tarjetaService = tarjetaService;
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

            /// caso 1: usa una tarjeta ya guardada
            if (dto.getTarjetaId() != null) {
                Tarjeta tarjeta = tarjetaRepository.findById(dto.getTarjetaId())
                        .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

                if (!tarjeta.getUsuario().getId().equals(usuarioId)) {
                    throw new RuntimeException("La tarjeta no pertenece al usuario");
                }
            }

            /// caso 2: usa una tarjeta nueva
            else {
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

                /// si el usuario quiere guardar la tarjeta nueva
                if (Boolean.TRUE.equals(dto.getGuardarTarjeta())) {
                    if (dto.getTipoTarjeta() == null) {
                        throw new RuntimeException("Debes indicar el tipo de tarjeta para guardarla");
                    }

                    GuardarTarjetaDTO guardarTarjetaDTO = new GuardarTarjetaDTO();
                    guardarTarjetaDTO.setTitular(nombreTitular);
                    guardarTarjetaDTO.setNumeroTarjeta(numeroTarjeta);
                    guardarTarjetaDTO.setFechaExpiracion(fechaExpiracion);
                    guardarTarjetaDTO.setTipo(dto.getTipoTarjeta());

                    tarjetaService.guardarTarjeta(usuarioId, guardarTarjetaDTO);
                }
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
            itemPedido.setTalla(itemCarrito.getTalla());
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

        String asunto = "Confirmación de pedido #" + pedidoGuardado.getId();
        String contenidoHtml = construirComprobanteHtml(usuario, pedidoGuardado, itemsPedido);

        try {
            emailService.enviarCorreoHtml(usuario.getEmail(), asunto, contenidoHtml);
            System.out.println("CORREO HTML DE PEDIDO ENVIADO A: " + usuario.getEmail());
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO HTML DE PEDIDO: " + e.getMessage());
            e.printStackTrace();
        }

        response.setPagoId(pagoGuardado.getId());
        response.setEstado(pagoGuardado.getEstado());
        response.setReferencia(pagoGuardado.getReferencia());
        response.setMensaje(pagoGuardado.getMensaje());
        response.setPedidoId(pedidoGuardado.getId());

        return response;
    }

    private String construirComprobanteHtml(Usuario usuario, Pedido pedido, List<ItemPedido> itemsPedido) {
        String plantilla = leerPlantillaHtml("templates/comprobante.html");

        return plantilla
                .replace("{{NOMBRE_USUARIO}}", usuario.getNombre())
                .replace("{{NUMERO_PEDIDO}}", String.valueOf(pedido.getId()))
                .replace("{{FECHA_PEDIDO}}", formatearFecha(pedido.getFechaPedido()))
                .replace("{{ESTADO_PEDIDO}}", "Confirmado")
                .replace("{{METODO_PAGO}}", formatearMetodoPago(pedido.getMetodoPago()))
                .replace("{{PRODUCTOS_HTML}}", construirProductosHtml(itemsPedido))
                .replace("{{TOTAL_PEDIDO}}", pedido.getTotal().toString());
    }

    private String construirProductosHtml(List<ItemPedido> itemsPedido) {
        StringBuilder sb = new StringBuilder();

        for (ItemPedido item : itemsPedido) {
            sb.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" ")
                    .append("style=\"border-collapse:collapse; margin-bottom:16px; background-color:#fafafa; border:1px solid #eaeaea; border-radius:10px;\">")
                    .append("<tr>")
                    .append("<td style=\"padding:18px;\">")

                    .append("<p style=\"margin:0 0 10px 0; font-size:16px; font-weight:bold; color:#111111;\">")
                    .append(item.getProducto().getNombre())
                    .append("</p>")

                    .append("<p style=\"margin:4px 0; font-size:14px; color:#444444;\"><strong>Cantidad:</strong> ")
                    .append(item.getCantidad())
                    .append("</p>")

                    .append("<p style=\"margin:4px 0; font-size:14px; color:#444444;\"><strong>Talla:</strong> ")
                    .append(item.getTalla() != null ? item.getTalla().toString() : "Sin talla")
                    .append("</p>")

                    .append("<p style=\"margin:4px 0; font-size:14px; color:#444444;\"><strong>Precio unitario:</strong> ")
                    .append(item.getPrecioUnitario())
                    .append(" €</p>")

                    .append("</td>")
                    .append("</tr>")
                    .append("</table>");
        }

        return sb.toString();
    }
    private String leerPlantillaHtml(String ruta) {
        try (InputStream inputStream = new ClassPathResource(ruta).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla HTML: " + ruta, e);
        }
    }

    private String formatearFecha(LocalDateTime fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fecha.format(formatter);
    }

    private String formatearMetodoPago(MetodoPago metodoPago) {
        return switch (metodoPago) {
            case TARJETA -> "Tarjeta";
            case PAYPAL -> "PayPal";
            case CONTRA_REEMBOLSO -> "Contra reembolso";
        };
    }
}