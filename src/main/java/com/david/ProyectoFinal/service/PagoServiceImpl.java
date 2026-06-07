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
import com.david.ProyectoFinal.model.Producto;
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
                .replace("{{NOMBRE_USUARIO}}", escaparHtml(textoSeguro(usuario.getNombre())))
                .replace("{{NUMERO_PEDIDO}}", String.valueOf(pedido.getId()))
                .replace("{{FECHA_PEDIDO}}", formatearFecha(pedido.getFechaPedido()))
                .replace("{{ESTADO_PEDIDO}}", formatearEstadoPedidoCorreo(pedido.getEstado()))
                .replace("{{METODO_PAGO}}", formatearMetodoPago(pedido.getMetodoPago()))
                .replace("{{METODO_ENTREGA}}", formatearMetodoEntrega(pedido.getMetodoEntrega()))
                .replace("{{TOTAL_ARTICULOS}}", String.valueOf(calcularTotalArticulos(itemsPedido)))
                .replace("{{ENTREGA_HTML}}", construirEntregaHtml(pedido))
                .replace("{{PRODUCTOS_HTML}}", construirProductosHtml(itemsPedido))
                .replace("{{TOTAL_PEDIDO}}", formatearImporteCorreo(pedido.getTotal()));
    }

    private String construirProductosHtml(List<ItemPedido> itemsPedido) {
        StringBuilder sb = new StringBuilder();

        for (ItemPedido item : itemsPedido) {
            Producto producto = item.getProducto();
            BigDecimal subtotal = item.getPrecioUnitario() != null && item.getCantidad() != null
                    ? item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
                    : BigDecimal.ZERO;

            sb.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" ")
                    .append("style=\"border-collapse:separate; margin-bottom:16px; background-color:#fcf8f3; border:1px solid #eadfce; border-radius:18px;\">")
                    .append("<tr>")
                    .append("<td style=\"padding:18px; width:112px; vertical-align:top;\">")
                    .append(construirImagenProductoHtml(producto))
                    .append("</td>")
                    .append("<td style=\"padding:18px 20px 18px 0; vertical-align:top;\">")
                    .append("<p style=\"margin:0 0 6px 0; font-size:18px; line-height:1.4; font-weight:700; color:#2a211c;\">")
                    .append(escaparHtml(producto != null && producto.getNombre() != null && !producto.getNombre().isBlank()
                            ? producto.getNombre().trim()
                            : "Producto"))
                    .append("</p>")
                    .append("<p style=\"margin:0 0 14px 0; font-size:12px; line-height:1.6; letter-spacing:0.4px; text-transform:uppercase; color:#8a7867;\">")
                    .append(escaparHtml(formatearSeccionCorreo(producto)))
                    .append("</p>")
                    .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;\">")
                    .append("<tr>")
                    .append("<td style=\"padding:0 0 7px 0; font-size:13px; color:#7a6755;\">Cantidad</td>")
                    .append("<td align=\"right\" style=\"padding:0 0 7px 0; font-size:13px; color:#2a211c; font-weight:700;\">")
                    .append(item.getCantidad())
                    .append("</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("<td style=\"padding:0 0 7px 0; font-size:13px; color:#7a6755;\">Talla</td>")
                    .append("<td align=\"right\" style=\"padding:0 0 7px 0; font-size:13px; color:#2a211c; font-weight:700;\">")
                    .append(escaparHtml(item.getTalla() != null ? item.getTalla().toString() : "Sin talla"))
                    .append("</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("<td style=\"padding:0 0 7px 0; font-size:13px; color:#7a6755;\">Precio unitario</td>")
                    .append("<td align=\"right\" style=\"padding:0 0 7px 0; font-size:13px; color:#2a211c; font-weight:700;\">")
                    .append(formatearImporteCorreo(item.getPrecioUnitario()))
                    .append(" &euro;</td>")
                    .append("</tr>")
                    .append("<tr>")
                    .append("<td style=\"padding:10px 0 0 0; font-size:13px; color:#7a6755; border-top:1px solid #eadfce;\">Subtotal</td>")
                    .append("<td align=\"right\" style=\"padding:10px 0 0 0; font-size:15px; color:#2a211c; font-weight:700; border-top:1px solid #eadfce;\">")
                    .append(formatearImporteCorreo(subtotal))
                    .append(" &euro;</td>")
                    .append("</tr>")
                    .append("</table>")
                    .append("</td>")
                    .append("</tr>")
                    .append("</table>");
        }

        return sb.toString();
    }

    private String construirEntregaHtml(Pedido pedido) {
        String detalleEntrega;

        if (pedido.getMetodoEntrega() == MetodoEntrega.DOMICILIO) {
            detalleEntrega = construirTextoDireccion(pedido.getDireccionEnvio());
        } else if (pedido.getMetodoEntrega() == MetodoEntrega.RECOGIDA_TIENDA) {
            detalleEntrega = construirTextoEstablecimiento(pedido.getEstablecimientoRecogida());
        } else if (pedido.getMetodoEntrega() == MetodoEntrega.PUNTO_RECOGIDA) {
            detalleEntrega = construirTextoPuntoRecogida(pedido.getPuntoRecogida());
        } else {
            detalleEntrega = "Te avisaremos con los detalles de entrega en cuanto haya novedades.";
        }

        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" "
                + "style=\"border-collapse:separate; margin:0 0 24px 0; background-color:#f8f2eb; border:1px solid #eadfce; border-radius:18px;\">"
                + "<tr><td style=\"padding:22px;\">"
                + "<p style=\"margin:0 0 8px 0; font-size:13px; letter-spacing:0.8px; text-transform:uppercase; color:#8a7867;\">Entrega</p>"
                + "<p style=\"margin:0 0 8px 0; font-size:17px; line-height:1.5; font-weight:700; color:#2a211c;\">"
                + escaparHtml(formatearMetodoEntrega(pedido.getMetodoEntrega()))
                + "</p>"
                + "<p style=\"margin:0; font-size:14px; line-height:1.7; color:#5f5144;\">"
                + escaparHtml(detalleEntrega)
                + "</p>"
                + "</td></tr></table>";
    }

    private String construirTextoDireccion(Direccion direccion) {
        if (direccion == null) {
            return "Direccion pendiente de confirmar.";
        }

        StringBuilder sb = new StringBuilder();
        appendSegment(sb, direccion.getAlias());
        appendSegment(sb, direccion.getCalle());
        appendSegment(sb, direccion.getNumero());

        StringBuilder extra = new StringBuilder();
        if (direccion.getPiso() != null && !direccion.getPiso().isBlank()) {
            extra.append("Piso ").append(direccion.getPiso().trim());
        }
        if (direccion.getPuerta() != null && !direccion.getPuerta().isBlank()) {
            if (extra.length() > 0) {
                extra.append(", ");
            }
            extra.append("Puerta ").append(direccion.getPuerta().trim());
        }
        appendSegment(sb, extra.toString());
        appendSegment(sb, direccion.getCodigoPostal());
        appendSegment(sb, direccion.getMunicipio());
        appendSegment(sb, direccion.getProvincia());

        return sb.length() > 0 ? sb.toString() : "Direccion pendiente de confirmar.";
    }

    private String construirTextoEstablecimiento(Establecimiento establecimiento) {
        if (establecimiento == null) {
            return "Recogida en tienda pendiente de confirmar.";
        }

        StringBuilder sb = new StringBuilder();
        appendSegment(sb, establecimiento.getNombre());
        appendSegment(sb, establecimiento.getDireccion());
        appendSegment(sb, establecimiento.getCiudad());
        appendSegment(sb, establecimiento.getProvincia());

        return sb.length() > 0 ? sb.toString() : "Recogida en tienda pendiente de confirmar.";
    }

    private String construirTextoPuntoRecogida(PuntoRecogida puntoRecogida) {
        if (puntoRecogida == null) {
            return "Punto de recogida pendiente de confirmar.";
        }

        StringBuilder sb = new StringBuilder();
        appendSegment(sb, puntoRecogida.getNombre());
        appendSegment(sb, puntoRecogida.getDireccion());
        appendSegment(sb, puntoRecogida.getCiudad());
        appendSegment(sb, puntoRecogida.getProvincia());

        return sb.length() > 0 ? sb.toString() : "Punto de recogida pendiente de confirmar.";
    }

    private void appendSegment(StringBuilder sb, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(" - ");
        }

        sb.append(valor.trim());
    }

    private int calcularTotalArticulos(List<ItemPedido> itemsPedido) {
        return itemsPedido.stream()
                .map(ItemPedido::getCantidad)
                .filter(cantidad -> cantidad != null && cantidad > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String construirImagenProductoHtml(Producto producto) {
        String urlImagen = obtenerUrlImagenProducto(producto);

        if (urlImagen != null) {
            return "<img src=\"" + escaparHtml(urlImagen) + "\" alt=\"Producto\" "
                    + "style=\"display:block; width:94px; height:118px; object-fit:cover; border:0; border-radius:14px; background-color:#efe6dc;\">";
        }

        return "<table role=\"presentation\" width=\"94\" cellspacing=\"0\" cellpadding=\"0\" "
                + "style=\"width:94px; height:118px; border-collapse:separate; background-color:#efe6dc; border-radius:14px;\">"
                + "<tr><td align=\"center\" style=\"padding:12px; font-size:11px; line-height:1.5; text-transform:uppercase; letter-spacing:0.8px; color:#8a7867;\">Sin imagen</td></tr>"
                + "</table>";
    }

    private String obtenerUrlImagenProducto(Producto producto) {
        if (producto == null) {
            return null;
        }

        if (producto.getUrlImagen() != null && producto.getUrlImagen().matches("^https?://.+")) {
            return producto.getUrlImagen().trim();
        }

        if (producto.getImagenes() != null) {
            for (var imagen : producto.getImagenes()) {
                if (imagen != null && imagen.getUrlImagen() != null && imagen.getUrlImagen().matches("^https?://.+")) {
                    return imagen.getUrlImagen().trim();
                }
            }
        }

        return null;
    }

    private String formatearSeccionCorreo(Producto producto) {
        if (producto == null) {
            return "Producto";
        }

        StringBuilder sb = new StringBuilder();

        if (producto.getTienda() != null && producto.getTienda().getNombre() != null && !producto.getTienda().getNombre().isBlank()) {
            sb.append(producto.getTienda().getNombre().trim());
        }

        if (producto.getCategoria() != null && producto.getCategoria().getNombre() != null && !producto.getCategoria().getNombre().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(producto.getCategoria().getNombre().trim());
        }

        if (producto.getSeccion() != null) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(switch (producto.getSeccion()) {
                case HOMBRE -> "Hombre";
                case MUJER -> "Mujer";
                case NINA -> "Nina";
                case NINO -> "Nino";
                case BEBE -> "Bebe";
                case UNISEX -> "Unisex";
            });
        }

        return sb.length() > 0 ? sb.toString() : "Producto";
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
        if (metodoPago == null) {
            return "No indicado";
        }

        return switch (metodoPago) {
            case TARJETA -> "Tarjeta";
            case PAYPAL -> "PayPal";
            case CONTRA_REEMBOLSO -> "Contra reembolso";
        };
    }

    private String formatearMetodoEntrega(MetodoEntrega metodoEntrega) {
        if (metodoEntrega == null) {
            return "Sin metodo definido";
        }

        return switch (metodoEntrega) {
            case DOMICILIO -> "Envio a domicilio";
            case RECOGIDA_TIENDA -> "Recogida en tienda";
            case PUNTO_RECOGIDA -> "Punto de recogida";
        };
    }

    private String formatearEstadoPedidoCorreo(EstadoPedido estadoPedido) {
        if (estadoPedido == null) {
            return "Sin estado";
        }

        return switch (estadoPedido) {
            case PENDIENTE -> "Pendiente";
            case CONFIRMADO -> "Confirmado";
            case PREPARANDO -> "Preparando";
            case ENVIADO -> "Enviado";
            case LISTO_PARA_RECOGER -> "Listo para recoger";
            case PENDIENTE_CONFIRMACION_ENTREGA -> "Pendiente de confirmacion de entrega";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }

    private String formatearImporteCorreo(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }

        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    private String textoSeguro(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Cliente";
        }

        return texto.trim();
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
