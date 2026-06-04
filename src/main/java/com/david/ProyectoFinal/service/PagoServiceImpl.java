package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.GuardarTarjetaDTO;
import com.david.ProyectoFinal.dto.PagoRequestDTO;
import com.david.ProyectoFinal.dto.PagoResponseDTO;
import com.david.ProyectoFinal.model.Carrito;
import com.david.ProyectoFinal.model.Direccion;
import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.model.EstadoPago;
import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.model.ItemPedido;
import com.david.ProyectoFinal.model.MetodoEntrega;
import com.david.ProyectoFinal.model.MetodoPago;
import com.david.ProyectoFinal.model.Pago;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.model.PuntoRecogida;
import com.david.ProyectoFinal.model.Tarjeta;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.CarritoRepository;
import com.david.ProyectoFinal.repository.DireccionRepository;
import com.david.ProyectoFinal.repository.EstablecimientoRepository;
import com.david.ProyectoFinal.repository.ItemCarritoRepository;
import com.david.ProyectoFinal.repository.ItemPedidoRepository;
import com.david.ProyectoFinal.repository.PagoRepository;
import com.david.ProyectoFinal.repository.PedidoRepository;
import com.david.ProyectoFinal.repository.PuntoRecogidaRepository;
import com.david.ProyectoFinal.repository.TarjetaRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
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
    private final DireccionRepository direccionRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final PuntoRecogidaRepository puntoRecogidaRepository;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           UsuarioRepository usuarioRepository,
                           CarritoRepository carritoRepository,
                           ItemCarritoRepository itemCarritoRepository,
                           PedidoRepository pedidoRepository,
                           ItemPedidoRepository itemPedidoRepository,
                           EmailService emailService,
                           TarjetaRepository tarjetaRepository,
                           TarjetaService tarjetaService,
                           DireccionRepository direccionRepository,
                           EstablecimientoRepository establecimientoRepository,
                           PuntoRecogidaRepository puntoRecogidaRepository) {
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.emailService = emailService;
        this.tarjetaRepository = tarjetaRepository;
        this.tarjetaService = tarjetaService;
        this.direccionRepository = direccionRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.puntoRecogidaRepository = puntoRecogidaRepository;
    }

    public PagoResponseDTO procesarPago(PagoRequestDTO dto) {

        PagoResponseDTO response = new PagoResponseDTO();

        Long usuarioId = dto.getUsuarioId();
        MetodoPago metodoPago = dto.getMetodoPago();
        MetodoEntrega metodoEntrega = dto.getMetodoEntrega();

        if (metodoPago == null) {
            throw new RuntimeException("El metodo de pago es obligatorio");
        }

        if (metodoEntrega == null) {
            throw new RuntimeException("El metodo de entrega es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(carrito.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("El carrito esta vacio");
        }

        BigDecimal total = items.stream()
                .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Direccion direccionEnvio = null;
        Establecimiento establecimientoRecogida = null;
        PuntoRecogida puntoRecogida = null;

        if (metodoEntrega == MetodoEntrega.DOMICILIO) {
            if (dto.getDireccionId() == null) {
                throw new RuntimeException("Debes indicar una direccion para el envio a domicilio");
            }

            direccionEnvio = direccionRepository.findById(dto.getDireccionId())
                    .orElseThrow(() -> new RuntimeException("Direccion no encontrada"));

            if (direccionEnvio.getUsuario() == null || !direccionEnvio.getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("La direccion no pertenece al usuario");
            }
        }

        if (metodoEntrega == MetodoEntrega.RECOGIDA_TIENDA) {
            if (dto.getEstablecimientoId() == null) {
                throw new RuntimeException("Debes indicar un establecimiento para la recogida en tienda");
            }

            establecimientoRecogida = establecimientoRepository.findById(dto.getEstablecimientoId())
                    .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));

            if (!Boolean.TRUE.equals(establecimientoRecogida.getDisponible())) {
                throw new RuntimeException("El establecimiento seleccionado no esta disponible");
            }
        }

        if (metodoEntrega == MetodoEntrega.PUNTO_RECOGIDA) {
            if (dto.getPuntoRecogidaId() == null) {
                throw new RuntimeException("Debes indicar un punto de recogida");
            }

            puntoRecogida = puntoRecogidaRepository.findById(dto.getPuntoRecogidaId())
                    .orElseThrow(() -> new RuntimeException("Punto de recogida no encontrado"));

            if (!Boolean.TRUE.equals(puntoRecogida.getDisponible())) {
                throw new RuntimeException("El punto de recogida seleccionado no esta disponible");
            }
        }

        if (metodoPago == MetodoPago.TARJETA) {
            if (dto.getTarjetaId() != null) {
                Tarjeta tarjeta = tarjetaRepository.findById(dto.getTarjetaId())
                        .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

                if (!tarjeta.getUsuario().getId().equals(usuarioId)) {
                    throw new RuntimeException("La tarjeta no pertenece al usuario");
                }
            } else {
                String numeroTarjeta = dto.getNumeroTarjeta();
                String nombreTitular = dto.getNombreTitular();
                String fechaExpiracion = dto.getFechaExpiracion();
                String cvv = dto.getCvv();

                if (numeroTarjeta == null || numeroTarjeta.isBlank()) {
                    throw new RuntimeException("El numero de tarjeta es obligatorio");
                }

                if (nombreTitular == null || nombreTitular.isBlank()) {
                    throw new RuntimeException("El nombre del titular es obligatorio");
                }

                if (fechaExpiracion == null || fechaExpiracion.isBlank()) {
                    throw new RuntimeException("La fecha de expiracion es obligatoria");
                }

                if (cvv == null || cvv.isBlank()) {
                    throw new RuntimeException("El CVV es obligatorio");
                }

                if (numeroTarjeta.length() < 12) {
                    throw new RuntimeException("Numero de tarjeta invalido");
                }

                boolean aprobado = !numeroTarjeta.endsWith("0");

                if (!aprobado) {
                    response.setEstado(EstadoPago.RECHAZADO);
                    response.setMensaje("Pago rechazado (tarjeta invalida)");
                    return response;
                }

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
                throw new RuntimeException("El email de PayPal no es valido");
            }
        }

        if (metodoPago == MetodoPago.CONTRA_REEMBOLSO) {
            Double importeEntrega = dto.getImporteEntrega();

            if (importeEntrega == null) {
                throw new RuntimeException("Debes indicar con cuanto vas a pagar");
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
        pedido.setMetodoEntrega(metodoEntrega);
        pedido.setDireccionEnvio(direccionEnvio);
        pedido.setEstablecimientoRecogida(establecimientoRecogida);
        pedido.setPuntoRecogida(puntoRecogida);

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
            pago.setMensaje("Pedido confirmado. Cambio a devolver: " + String.format("%.2f", cambio) + " EUR");
        }

        Pago pagoGuardado = pagoRepository.save(pago);

        String asunto = "Confirmacion de pedido #" + pedidoGuardado.getId();
        String contenidoHtml = construirComprobanteHtml(usuario, pedidoGuardado, itemsPedido);

        EmailDispatchResult resultadoCorreo;

        try {
            resultadoCorreo = emailService.enviarCorreoHtmlConResultado(usuario.getEmail(), asunto, contenidoHtml);
            System.out.println("CORREO HTML DE PEDIDO ENVIADO A: " + usuario.getEmail());
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO HTML DE PEDIDO: " + e.getMessage());
            e.printStackTrace();
            resultadoCorreo = EmailDispatchResult.pendiente(
                    "El pedido ya esta confirmado. Te enviaremos el comprobante por correo en cuanto vuelva el servicio."
            );
        }

        response.setPagoId(pagoGuardado.getId());
        response.setEstado(pagoGuardado.getEstado());
        response.setReferencia(pagoGuardado.getReferencia());
        response.setMensaje(pagoGuardado.getMensaje());
        response.setPedidoId(pedidoGuardado.getId());
        response.setCorreoPendiente(resultadoCorreo.isPendiente());
        response.setMensajeCorreo(resultadoCorreo.getMensaje());

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
                .replace("{{TOTAL_PEDIDO}}", formatearImporteCorreo(pedido.getTotal()));
    }

    private String construirProductosHtml(List<ItemPedido> itemsPedido) {
        StringBuilder sb = new StringBuilder();

        for (ItemPedido item : itemsPedido) {
            sb.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" ")
                    .append("style=\"border-collapse:separate; margin-bottom:14px; background-color:#fcf8f3; border:1px solid #eadfce; border-radius:16px;\">")
                    .append("<tr>")
                    .append("<td style=\"padding:18px 20px;\">")
                    .append("<p style=\"margin:0 0 12px 0; font-size:16px; font-weight:bold; color:#2a211c;\">")
                    .append(item.getProducto().getNombre())
                    .append("</p>")
                    .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;\">")
                    .append("<tr>")
                    .append("<td style=\"padding:0 0 6px 0; font-size:13px; color:#7a6755;\">Cantidad</td>")
                    .append("<td align=\"right\" style=\"padding:0 0 6px 0; font-size:13px; color:#2a211c; font-weight:bold;\">")
                    .append(item.getCantidad())
                    .append("</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("<td style=\"padding:0 0 6px 0; font-size:13px; color:#7a6755;\">Talla</td>")
                    .append("<td align=\"right\" style=\"padding:0 0 6px 0; font-size:13px; color:#2a211c; font-weight:bold;\">")
                    .append(item.getTalla() != null ? item.getTalla().toString() : "Sin talla")
                    .append("</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("<td style=\"padding:0; font-size:13px; color:#7a6755;\">Precio unitario</td>")
                    .append("<td align=\"right\" style=\"padding:0; font-size:13px; color:#2a211c; font-weight:bold;\">")
                    .append(formatearImporteCorreo(item.getPrecioUnitario()))
                    .append(" &euro;</td>")
                    .append("</tr>")
                    .append("</table>")
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

    private String formatearImporteCorreo(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }

        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}
