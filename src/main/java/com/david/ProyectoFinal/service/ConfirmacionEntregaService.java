package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.ConfirmacionEntregaResponseDTO;
import com.david.ProyectoFinal.dto.ConfirmacionEntregaValidacionResponseDTO;
import com.david.ProyectoFinal.model.ConfirmacionEntrega;
import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.MetodoEntrega;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.repository.ConfirmacionEntregaRepository;
import com.david.ProyectoFinal.repository.PedidoRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfirmacionEntregaService {

    /// dependencias
    private final ConfirmacionEntregaRepository confirmacionEntregaRepository;
    private final PedidoRepository pedidoRepository;
    private final QrService qrService;
    private final EmailService emailService;

    public ConfirmacionEntregaService(ConfirmacionEntregaRepository confirmacionEntregaRepository,
                                      PedidoRepository pedidoRepository,
                                      QrService qrService,
                                      EmailService emailService) {
        this.confirmacionEntregaRepository = confirmacionEntregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.qrService = qrService;
        this.emailService = emailService;
    }

    /// crea una confirmación nueva para un pedido
    public ConfirmacionEntrega crearConfirmacionParaPedido(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        /// solo tiene sentido generar QR cuando el pedido está pendiente de confirmación de entrega
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA) {
            throw new RuntimeException("Solo se puede generar confirmación para pedidos pendientes de confirmación de entrega");
        }

        /// si ya existe una confirmación previa, la invalidamos antes de crear una nueva
        Optional<ConfirmacionEntrega> existente = confirmacionEntregaRepository.findByPedidoId(pedidoId);

        if (existente.isPresent()) {
            ConfirmacionEntrega anterior = existente.get();
            anterior.setActivo(false);
            confirmacionEntregaRepository.save(anterior);
        }

        ConfirmacionEntrega confirmacionEntrega = new ConfirmacionEntrega();
        confirmacionEntrega.setPedido(pedido);
        confirmacionEntrega.setToken(generarTokenUnico());
        confirmacionEntrega.setFechaCreacion(LocalDateTime.now());
        confirmacionEntrega.setFechaExpiracion(LocalDateTime.now().plusDays(7));
        confirmacionEntrega.setUsado(false);
        confirmacionEntrega.setFechaUso(null);
        confirmacionEntrega.setActivo(true);

        ConfirmacionEntrega confirmacionGuardada = confirmacionEntregaRepository.save(confirmacionEntrega);

        /// tras crear la confirmación, se envía el correo con el QR
        enviarCorreoConQr(confirmacionGuardada);

        return confirmacionGuardada;
    }

    /// valida un token sin cerrar todavía el pedido
    public ConfirmacionEntrega validarToken(String token) {
        ConfirmacionEntrega confirmacionEntrega = confirmacionEntregaRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de confirmación no encontrado"));

        if (!Boolean.TRUE.equals(confirmacionEntrega.getActivo())) {
            throw new RuntimeException("El código QR ya no está activo");
        }

        if (Boolean.TRUE.equals(confirmacionEntrega.getUsado())) {
            throw new RuntimeException("El código QR ya fue utilizado");
        }

        if (confirmacionEntrega.getFechaExpiracion() != null &&
                LocalDateTime.now().isAfter(confirmacionEntrega.getFechaExpiracion())) {
            throw new RuntimeException("El código QR ha expirado");
        }

        if (confirmacionEntrega.getPedido() == null) {
            throw new RuntimeException("La confirmación no está asociada a ningún pedido");
        }

        if (confirmacionEntrega.getPedido().getEstado() != EstadoPedido.PENDIENTE_CONFIRMACION_ENTREGA) {
            throw new RuntimeException("El pedido no está en un estado válido para confirmar la entrega");
        }

        return confirmacionEntrega;
    }

    /// valida el token y cierra el pedido como entregado
    public Pedido confirmarEntregaConToken(String token) {
        ConfirmacionEntrega confirmacionEntrega = validarToken(token);

        Pedido pedido = confirmacionEntrega.getPedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedido);

        confirmacionEntrega.setUsado(true);
        confirmacionEntrega.setFechaUso(LocalDateTime.now());
        confirmacionEntrega.setActivo(false);
        confirmacionEntregaRepository.save(confirmacionEntrega);

        return pedido;
    }

    /// devuelve la confirmación de un pedido
    public ConfirmacionEntrega obtenerPorPedido(Long pedidoId) {
        return confirmacionEntregaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No existe confirmación para ese pedido"));
    }

    /// genera un token aleatorio y comprueba que no exista ya en base de datos
    private String generarTokenUnico() {
        String token;

        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (confirmacionEntregaRepository.findByToken(token).isPresent());

        return token;
    }

    /// construye y envía el correo con el QR embebido
    private void enviarCorreoConQr(ConfirmacionEntrega confirmacionEntrega) {
        Pedido pedido = confirmacionEntrega.getPedido();

        if (pedido == null || pedido.getUsuario() == null || pedido.getUsuario().getEmail() == null) {
            throw new RuntimeException("No se puede enviar el correo de confirmación de entrega");
        }

        /// el QR contiene solo el token, no una URL protegida del backend
        String contenidoQr = construirContenidoQr(confirmacionEntrega.getToken());
        byte[] qrPng = qrService.generarQrComoPng(contenidoQr, 320, 320);

        String asunto = "Código QR para la entrega del pedido #" + pedido.getId();
        String contenidoHtml = construirCorreoHtmlConfirmacion(confirmacionEntrega);

        emailService.enviarCorreoHtmlConImagenInline(
                pedido.getUsuario().getEmail(),
                asunto,
                contenidoHtml,
                qrPng,
                "qrEntrega",
                "qr-pedido-" + pedido.getId() + ".png"
        );
    }

    /// el contenido del QR será el token único de confirmación
    private String construirContenidoQr(String token) {
        return token;
    }

    /// construye el contenido html del correo a partir de la plantilla
    private String construirCorreoHtmlConfirmacion(ConfirmacionEntrega confirmacionEntrega) {
        Pedido pedido = confirmacionEntrega.getPedido();

        String plantilla = leerPlantillaHtml("templates/confirmacionEntrega.html");

        String nombreUsuario = pedido.getUsuario() != null && pedido.getUsuario().getNombre() != null
                ? pedido.getUsuario().getNombre()
                : "cliente";

        String textoMetodoEntrega = formatearMetodoEntrega(pedido.getMetodoEntrega());
        String fechaDisponible = formatearFecha(
                confirmacionEntrega.getFechaCreacion() != null
                        ? confirmacionEntrega.getFechaCreacion().plusDays(1)
                        : LocalDateTime.now().plusDays(1)
        );
        String textoDisponibilidad = construirTextoDisponibilidad(pedido, fechaDisponible);

        return plantilla
                .replace("{{NOMBRE_USUARIO}}", nombreUsuario)
                .replace("{{NUMERO_PEDIDO}}", String.valueOf(pedido.getId()))
                .replace("{{ESTADO_PEDIDO}}", pedido.getEstado().name())
                .replace("{{METODO_ENTREGA}}", textoMetodoEntrega)
                .replace("{{TOTAL_PEDIDO}}", formatearBigDecimal(pedido.getTotal()))
                .replace("{{INFO_ENTREGA}}", textoDisponibilidad)
                .replace("{{FECHA_EXPIRACION}}", formatearFecha(confirmacionEntrega.getFechaExpiracion()));
    }

    private String construirTextoDisponibilidad(Pedido pedido, String fechaDisponible) {
        if (pedido.getMetodoEntrega() == MetodoEntrega.DOMICILIO) {
            return "Entrega prevista a domicilio para " + fechaDisponible;
        }

        if (pedido.getMetodoEntrega() == MetodoEntrega.RECOGIDA_TIENDA) {
            String nombreEstablecimiento = pedido.getEstablecimientoRecogida() != null
                    ? pedido.getEstablecimientoRecogida().getNombre()
                    : "tu tienda seleccionada";
            return "Disponible para recoger en " + nombreEstablecimiento + " a partir de " + fechaDisponible;
        }

        if (pedido.getMetodoEntrega() == MetodoEntrega.PUNTO_RECOGIDA) {
            String nombrePunto = pedido.getPuntoRecogida() != null
                    ? pedido.getPuntoRecogida().getNombre()
                    : "tu punto de recogida";
            return "Disponible en " + nombrePunto + " a partir de " + fechaDisponible;
        }

        return "Disponible a partir de " + fechaDisponible;
    }

    private String formatearMetodoEntrega(MetodoEntrega metodoEntrega) {
        if (metodoEntrega == null) {
            return "No indicado";
        }

        return switch (metodoEntrega) {
            case DOMICILIO -> "Envío a domicilio";
            case RECOGIDA_TIENDA -> "Recogida en tienda";
            case PUNTO_RECOGIDA -> "Punto de recogida";
        };
    }

    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "Sin fecha";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fecha.format(formatter);
    }

    private String formatearBigDecimal(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }

        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    private String leerPlantillaHtml(String ruta) {
        try (InputStream inputStream = new ClassPathResource(ruta).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla HTML: " + ruta, e);
        }
    }

    private ConfirmacionEntregaResponseDTO convertirAResponseDTO(ConfirmacionEntrega confirmacionEntrega) {
        return new ConfirmacionEntregaResponseDTO(
                confirmacionEntrega.getId(),
                confirmacionEntrega.getPedido() != null ? confirmacionEntrega.getPedido().getId() : null,
                confirmacionEntrega.getToken(),
                confirmacionEntrega.getFechaCreacion(),
                confirmacionEntrega.getFechaExpiracion(),
                confirmacionEntrega.getUsado(),
                confirmacionEntrega.getFechaUso(),
                confirmacionEntrega.getActivo()
        );
    }

    public ConfirmacionEntregaResponseDTO crearConfirmacionParaPedidoDTO(Long pedidoId) {
        return convertirAResponseDTO(crearConfirmacionParaPedido(pedidoId));
    }

    public ConfirmacionEntregaResponseDTO validarTokenDTO(String token) {
        return convertirAResponseDTO(validarToken(token));
    }

    public ConfirmacionEntregaResponseDTO obtenerPorPedidoDTO(Long pedidoId) {
        return convertirAResponseDTO(obtenerPorPedido(pedidoId));
    }

    public ConfirmacionEntregaValidacionResponseDTO confirmarEntregaConTokenDTO(String token) {
        Pedido pedido = confirmarEntregaConToken(token);

        return new ConfirmacionEntregaValidacionResponseDTO(
                pedido.getId(),
                pedido.getEstado().name(),
                "Entrega confirmada correctamente",
                true
        );
    }
}