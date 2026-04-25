package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.Direccion;
import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.model.ItemPedido;
import com.david.ProyectoFinal.model.MetodoEntrega;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.model.PuntoRecogida;
import com.david.ProyectoFinal.repository.ItemPedidoRepository;
import com.david.ProyectoFinal.repository.PedidoRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    /// dependencias
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EmailService emailService;
    private final QrService qrService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ItemPedidoRepository itemPedidoRepository,
                         EmailService emailService,
                         QrService qrService) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.emailService = emailService;
        this.qrService = qrService;
    }

    public List<Pedido> obtenerPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO ||
                pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya finalizado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorUsuarioYEstado(Long usuarioId, EstadoPedido estado) {
        return pedidoRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }

    public List<ItemPedido> obtenerItemsDePedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> obtenerPedidosPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido cambiarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido estadoActual = pedido.getEstado();

        if (estadoActual == EstadoPedido.ENTREGADO || estadoActual == EstadoPedido.CANCELADO) {
            throw new RuntimeException("No se puede modificar un pedido ya finalizado");
        }

        List<EstadoPedido> estadosValidos = calcularSiguientesEstadosValidos(pedido);

        if (!estadosValidos.contains(nuevoEstado)) {
            throw new RuntimeException("Cambio de estado no permitido para el método de entrega del pedido");
        }

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA) {
            prepararYEnviarCorreoConfirmacionEntrega(pedido);
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido confirmarEntregaPorToken(String tokenConfirmacionEntrega) {
        if (tokenConfirmacionEntrega == null || tokenConfirmacionEntrega.isBlank()) {
            throw new RuntimeException("Token de confirmación no válido");
        }

        Pedido pedido = pedidoRepository.findByTokenConfirmacionEntrega(tokenConfirmacionEntrega)
                .orElseThrow(() -> new RuntimeException("Código QR no válido"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("El pedido ya está entregado");
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("No se puede confirmar un pedido cancelado");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA) {
            throw new RuntimeException("El pedido todavía no está pendiente de confirmación de entrega");
        }

        if (pedido.getFechaExpiracionConfirmacionEntrega() == null ||
                pedido.getFechaExpiracionConfirmacionEntrega().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código QR ha expirado");
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaConfirmacionEntrega(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    public List<EstadoPedido> obtenerSiguientesEstadosValidos(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        return calcularSiguientesEstadosValidos(pedido);
    }

    private void prepararYEnviarCorreoConfirmacionEntrega(Pedido pedido) {
        if (Boolean.TRUE.equals(pedido.getCorreoConfirmacionEntregaEnviado())) {
            return;
        }

        if (pedido.getUsuario() == null || pedido.getUsuario().getEmail() == null || pedido.getUsuario().getEmail().isBlank()) {
            throw new RuntimeException("El pedido no tiene un email de usuario válido");
        }

        if (pedido.getTokenConfirmacionEntrega() == null || pedido.getTokenConfirmacionEntrega().isBlank()) {
            pedido.setTokenConfirmacionEntrega(UUID.randomUUID().toString());
        }

        if (pedido.getFechaExpiracionConfirmacionEntrega() == null) {
            pedido.setFechaExpiracionConfirmacionEntrega(LocalDateTime.now().plusDays(7));
        }

        String asunto = "Confirmación de entrega del pedido #" + pedido.getId();
        String contenidoHtml = construirConfirmacionEntregaHtml(pedido);
        byte[] qrBytes = qrService.generarQrComoPng(pedido.getTokenConfirmacionEntrega(), 280, 280);

        emailService.enviarCorreoHtmlConImagenInline(
                pedido.getUsuario().getEmail(),
                asunto,
                contenidoHtml,
                qrBytes,
                "qrEntrega",
                "qr-entrega.png"
        );

        pedido.setCorreoConfirmacionEntregaEnviado(true);
    }

    private String construirConfirmacionEntregaHtml(Pedido pedido) {
        String plantilla = leerPlantillaHtml("templates/confirmacionEntrega.html");

        return plantilla
                .replace("{{NOMBRE_USUARIO}}", pedido.getUsuario().getNombre())
                .replace("{{NUMERO_PEDIDO}}", String.valueOf(pedido.getId()))
                .replace("{{ESTADO_PEDIDO}}", formatearEstadoPedido(pedido.getEstado()))
                .replace("{{METODO_ENTREGA}}", formatearMetodoEntrega(pedido.getMetodoEntrega()))
                .replace("{{TOTAL_PEDIDO}}", pedido.getTotal().toString())
                .replace("{{INFO_ENTREGA}}", construirInfoEntrega(pedido))
                .replace("{{FECHA_EXPIRACION}}", formatearFecha(pedido.getFechaExpiracionConfirmacionEntrega()));
    }

    private String construirInfoEntrega(Pedido pedido) {
        if (pedido.getMetodoEntrega() == MetodoEntrega.DOMICILIO) {
            return construirInfoDireccion(pedido.getDireccionEnvio());
        }

        if (pedido.getMetodoEntrega() == MetodoEntrega.RECOGIDA_TIENDA) {
            return construirInfoEstablecimiento(pedido.getEstablecimientoRecogida());
        }

        if (pedido.getMetodoEntrega() == MetodoEntrega.PUNTO_RECOGIDA) {
            return construirInfoPuntoRecogida(pedido.getPuntoRecogida());
        }

        return "Información de entrega no disponible";
    }

    private String construirInfoDireccion(Direccion direccion) {
        if (direccion == null) {
            return "Dirección de envío no disponible";
        }

        StringBuilder sb = new StringBuilder();

        agregarParte(sb, direccion.getCalle());
        agregarParte(sb, direccion.getNumero());
        agregarParte(sb, direccion.getPiso());
        agregarParte(sb, direccion.getPuerta());
        agregarParte(sb, direccion.getCodigoPostal());
        agregarParte(sb, direccion.getMunicipio());
        agregarParte(sb, direccion.getProvincia());

        return sb.length() > 0 ? sb.toString() : "Dirección de envío no disponible";
    }

    private String construirInfoEstablecimiento(Establecimiento establecimiento) {
        if (establecimiento == null) {
            return "Establecimiento no disponible";
        }

        StringBuilder sb = new StringBuilder();

        agregarParte(sb, establecimiento.getNombre());
        agregarParte(sb, establecimiento.getDireccion());
        agregarParte(sb, establecimiento.getCiudad());
        agregarParte(sb, establecimiento.getProvincia());

        if (establecimiento.getTienda() != null) {
            agregarParte(sb, establecimiento.getTienda().getNombre());
        }

        return sb.length() > 0 ? sb.toString() : "Establecimiento no disponible";
    }

    private String construirInfoPuntoRecogida(PuntoRecogida puntoRecogida) {
        if (puntoRecogida == null) {
            return "Punto de recogida no disponible";
        }

        StringBuilder sb = new StringBuilder();

        agregarParte(sb, puntoRecogida.getNombre());
        agregarParte(sb, puntoRecogida.getDireccion());
        agregarParte(sb, puntoRecogida.getCiudad());
        agregarParte(sb, puntoRecogida.getProvincia());

        return sb.length() > 0 ? sb.toString() : "Punto de recogida no disponible";
    }

    private void agregarParte(StringBuilder sb, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(", ");
        }

        sb.append(valor);
    }

    private String leerPlantillaHtml(String ruta) {
        try (InputStream inputStream = new ClassPathResource(ruta).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla HTML: " + ruta, e);
        }
    }

    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "Sin fecha";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fecha.format(formatter);
    }

    private String formatearEstadoPedido(EstadoPedido estado) {
        if (estado == null) {
            return "Sin estado";
        }

        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case CONFIRMADO -> "Confirmado";
            case PREPARANDO -> "Preparando";
            case ENVIADO -> "Enviado";
            case LISTO_PARA_RECOGER -> "Listo para recoger";
            case PENDIENTE_CONFIRMACION_ENTREGA -> "Pendiente de confirmación de entrega";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }

    private String formatearMetodoEntrega(MetodoEntrega metodoEntrega) {
        if (metodoEntrega == null) {
            return "Sin método de entrega";
        }

        return switch (metodoEntrega) {
            case DOMICILIO -> "Domicilio";
            case RECOGIDA_TIENDA -> "Recogida en tienda";
            case PUNTO_RECOGIDA -> "Punto de recogida";
        };
    }

    private List<EstadoPedido> calcularSiguientesEstadosValidos(Pedido pedido) {
        EstadoPedido estadoActual = pedido.getEstado();
        MetodoEntrega metodoEntrega = pedido.getMetodoEntrega();

        if (estadoActual == null) {
            throw new RuntimeException("El pedido no tiene estado asignado");
        }

        if (metodoEntrega == null) {
            throw new RuntimeException("El pedido no tiene método de entrega asignado");
        }

        /// si ya está finalizado, no hay más estados posibles
        if (estadoActual == EstadoPedido.ENTREGADO || estadoActual == EstadoPedido.CANCELADO) {
            return List.of();
        }

        switch (metodoEntrega) {

            case DOMICILIO:
                switch (estadoActual) {
                    case CONFIRMADO:
                        return List.of(EstadoPedido.PREPARANDO);

                    case PREPARANDO:
                        return List.of(EstadoPedido.ENVIADO);

                    case ENVIADO:
                        return List.of(EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA);

                    case PENDIENTE_CONFIRMACION_ENTREGA:
                        return List.of();

                    default:
                        return List.of();
                }

            case RECOGIDA_TIENDA:
                switch (estadoActual) {
                    case CONFIRMADO:
                        return List.of(EstadoPedido.PREPARANDO);

                    case PREPARANDO:
                        return List.of(EstadoPedido.LISTO_PARA_RECOGER);

                    case LISTO_PARA_RECOGER:
                        return List.of(EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA);

                    case PENDIENTE_CONFIRMACION_ENTREGA:
                        return List.of();

                    default:
                        return List.of();
                }

            case PUNTO_RECOGIDA:
                switch (estadoActual) {
                    case CONFIRMADO:
                        return List.of(EstadoPedido.PREPARANDO);

                    case PREPARANDO:
                        return List.of(EstadoPedido.ENVIADO);

                    case ENVIADO:
                        return List.of(EstadoPedido.LISTO_PARA_RECOGER);

                    case LISTO_PARA_RECOGER:
                        return List.of(EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA);

                    case PENDIENTE_CONFIRMACION_ENTREGA:
                        return List.of();

                    default:
                        return List.of();
                }

            default:
                return List.of();
        }
    }

    public List<EstadoPedido> obtenerTodosLosEstadosPedido() {
        return List.of(
                EstadoPedido.PENDIENTE,
                EstadoPedido.CONFIRMADO,
                EstadoPedido.PREPARANDO,
                EstadoPedido.ENVIADO,
                EstadoPedido.LISTO_PARA_RECOGER,
                EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA,
                EstadoPedido.ENTREGADO,
                EstadoPedido.CANCELADO
        );
    }
}