package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambioPrecioProductoDTO;
import com.david.ProyectoFinal.dto.NotificacionUsuarioDTO;
import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.model.NotificacionUsuario;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.TipoCambioPrecio;
import com.david.ProyectoFinal.model.TipoNotificacionUsuario;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.FavoritoRepository;
import com.david.ProyectoFinal.repository.NotificacionUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificacionUsuarioService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacionUsuarioRepository notificacionUsuarioRepository;
    private final FavoritoRepository favoritoRepository;
    private final EmailService emailService;
    private final NotificacionPushService notificacionPushService;

    @Value("${app.frontend-url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.user-notifications.price-drop.enabled:true}")
    private boolean notificacionesBajadaPrecioEnabled;

    @Value("${app.user-notifications.email.enabled:false}")
    private boolean correoNotificacionEnabled;

    public NotificacionUsuarioService(NotificacionUsuarioRepository notificacionUsuarioRepository,
                                      FavoritoRepository favoritoRepository,
                                      EmailService emailService,
                                      NotificacionPushService notificacionPushService) {
        this.notificacionUsuarioRepository = notificacionUsuarioRepository;
        this.favoritoRepository = favoritoRepository;
        this.emailService = emailService;
        this.notificacionPushService = notificacionPushService;
    }

    public void crearNotificacionesPorCambioFavoritos(Producto producto, CambioPrecioProductoDTO cambioPrecio) {
        if (!notificacionesBajadaPrecioEnabled || producto == null || producto.getId() == null || cambioPrecio == null) {
            return;
        }

        if (cambioPrecio.getTipoCambio() != TipoCambioPrecio.BAJADA) {
            return;
        }

        List<Favorito> favoritos = favoritoRepository.findByProductoId(producto.getId());

        if (favoritos == null || favoritos.isEmpty()) {
            return;
        }

        Set<Long> usuariosNotificados = new LinkedHashSet<>();

        for (Favorito favorito : favoritos) {
            if (favorito == null || favorito.getUsuario() == null || favorito.getUsuario().getId() == null) {
                continue;
            }

            Usuario usuario = favorito.getUsuario();

            if (!usuariosNotificados.add(usuario.getId())) {
                continue;
            }

            NotificacionUsuario notificacion = construirNotificacion(usuario, producto, cambioPrecio);
            NotificacionUsuario guardada = notificacionUsuarioRepository.save(notificacion);

            if (correoNotificacionEnabled) {
                enviarCorreoNotificacion(usuario, guardada);
            }

            notificacionPushService.enviarNotificacion(usuario, guardada);
        }
    }

    public List<NotificacionUsuarioDTO> obtenerNotificacionesUsuario(Long usuarioId, int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 100));

        return notificacionUsuarioRepository.findByUsuarioIdOrderByLeidaAscFechaCreacionDesc(
                        usuarioId,
                        PageRequest.of(0, limiteSeguro)
                )
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public long contarNoLeidas(Long usuarioId) {
        return notificacionUsuarioRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public NotificacionUsuarioDTO marcarComoLeida(Long usuarioId, Long notificacionId) {
        NotificacionUsuario notificacion = notificacionUsuarioRepository.findByIdAndUsuarioId(notificacionId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));

        if (!Boolean.TRUE.equals(notificacion.getLeida())) {
            notificacion.setLeida(true);
            notificacion.setFechaLeida(LocalDateTime.now());
            notificacion = notificacionUsuarioRepository.save(notificacion);
        }

        return convertirADTO(notificacion);
    }

    @Transactional
    public int marcarTodasComoLeidas(Long usuarioId) {
        return notificacionUsuarioRepository.marcarTodasComoLeidas(usuarioId, LocalDateTime.now());
    }

    private NotificacionUsuario construirNotificacion(Usuario usuario,
                                                      Producto producto,
                                                      CambioPrecioProductoDTO cambioPrecio) {
        NotificacionUsuario notificacion = new NotificacionUsuario();
        notificacion.setUsuario(usuario);
        notificacion.setProducto(producto);
        notificacion.setTipo(Boolean.TRUE.equals(cambioPrecio.getRebajaMayor())
                ? TipoNotificacionUsuario.REBAJA_FAVORITO
                : TipoNotificacionUsuario.BAJADA_PRECIO_FAVORITO);
        notificacion.setTitulo(construirTitulo(cambioPrecio));
        notificacion.setMensaje(construirMensaje(cambioPrecio));
        notificacion.setUrlDestino(construirUrlDestino(producto));
        notificacion.setUrlProductoOriginal(cambioPrecio.getUrlProducto());
        notificacion.setLeida(false);
        notificacion.setEmailEnviado(false);
        notificacion.setFechaCreacion(cambioPrecio.getFechaCambio() == null ? LocalDateTime.now() : cambioPrecio.getFechaCambio());
        notificacion.setPrecioAnterior(cambioPrecio.getPrecioAnterior());
        notificacion.setPrecioNuevo(cambioPrecio.getPrecioNuevo());
        notificacion.setPorcentajeDescuentoNuevo(cambioPrecio.getPorcentajeDescuentoNuevo());
        notificacion.setRebajaMayor(Boolean.TRUE.equals(cambioPrecio.getRebajaMayor()));
        return notificacion;
    }

    private String construirTitulo(CambioPrecioProductoDTO cambioPrecio) {
        String nombreProducto = textoSeguro(cambioPrecio.getNombreProducto(), "Producto");

        if (Boolean.TRUE.equals(cambioPrecio.getRebajaMayor())) {
            return nombreProducto + " ahora tiene una rebaja mayor";
        }

        return nombreProducto + " ha bajado de precio";
    }

    private String construirMensaje(CambioPrecioProductoDTO cambioPrecio) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(textoSeguro(cambioPrecio.getNombreProducto(), "Producto"));
        mensaje.append(" en ");
        mensaje.append(textoSeguro(cambioPrecio.getTienda(), "tu tienda favorita"));
        mensaje.append(" ha pasado de ");
        mensaje.append(formatearImporte(cambioPrecio.getPrecioAnterior()));
        mensaje.append(" a ");
        mensaje.append(formatearImporte(cambioPrecio.getPrecioNuevo()));

        if (cambioPrecio.getPorcentajeDescuentoNuevo() != null) {
            mensaje.append(". Descuento actual: ");
            mensaje.append(cambioPrecio.getPorcentajeDescuentoNuevo());
            mensaje.append("%");
        }

        if (Boolean.TRUE.equals(cambioPrecio.getRebajaMayor())) {
            mensaje.append(". La rebaja es mayor que la anterior.");
        }

        return mensaje.toString();
    }

    private String construirUrlDestino(Producto producto) {
        if (producto == null || producto.getId() == null) {
            return null;
        }

        // Ruta relativa para que funcione desde PC, móvil y servidor.
        return "fichaProducto.html?id=" + producto.getId();
    }

    private void enviarCorreoNotificacion(Usuario usuario, NotificacionUsuario notificacion) {
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank() || notificacion == null) {
            return;
        }

        String asunto = notificacion.getTitulo();
        String contenidoHtml = construirContenidoHtmlCorreo(usuario, notificacion);

        emailService.enviarCorreoHtmlConResultado(usuario.getEmail(), asunto, contenidoHtml);

        notificacion.setEmailEnviado(true);
        notificacionUsuarioRepository.save(notificacion);
    }

    private String construirContenidoHtmlCorreo(Usuario usuario, NotificacionUsuario notificacion) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family:Arial,sans-serif; color:#1f2937; line-height:1.6;\">");
        html.append("<h2 style=\"margin:0 0 10px 0; color:#111827;\">").append(escaparHtml(notificacion.getTitulo())).append("</h2>");
        html.append("<p style=\"margin:0 0 16px 0;\">Hola ").append(escaparHtml(textoSeguro(usuario.getNombre(), "cliente"))).append(",</p>");
        html.append("<p style=\"margin:0 0 16px 0;\">").append(escaparHtml(notificacion.getMensaje())).append("</p>");
        html.append("<p style=\"margin:0 0 16px 0;\">");
        html.append("Fecha del cambio: <strong>").append(notificacion.getFechaCreacion() == null ? "-" : notificacion.getFechaCreacion().format(FORMATTER)).append("</strong>");
        html.append("</p>");

        if (notificacion.getUrlDestino() != null && !notificacion.getUrlDestino().isBlank()) {
            String urlCorreo = construirUrlAbsolutaCorreo(notificacion.getUrlDestino());
            html.append("<p style=\"margin:0 0 12px 0;\">");
            html.append("<a href=\"").append(escaparHtml(urlCorreo)).append("\" ");
            html.append("style=\"display:inline-block; padding:12px 18px; background:#111827; color:#ffffff; text-decoration:none; border-radius:999px; font-weight:700;\">");
            html.append("Ver producto en MODA");
            html.append("</a></p>");
        }

        if (notificacion.getUrlProductoOriginal() != null && !notificacion.getUrlProductoOriginal().isBlank()) {
            html.append("<p style=\"margin:0;\">");
            html.append("<a href=\"").append(escaparHtml(notificacion.getUrlProductoOriginal())).append("\" style=\"color:#6d28d9;\">Ir a la tienda original</a>");
            html.append("</p>");
        }

        html.append("</div>");
        return html.toString();
    }

    private NotificacionUsuarioDTO convertirADTO(NotificacionUsuario notificacion) {
        NotificacionUsuarioDTO dto = new NotificacionUsuarioDTO();
        dto.setId(notificacion.getId());
        dto.setProductoId(notificacion.getProducto() == null ? null : notificacion.getProducto().getId());
        dto.setTipo(notificacion.getTipo());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setUrlDestino(notificacion.getUrlDestino());
        dto.setUrlProductoOriginal(notificacion.getUrlProductoOriginal());
        dto.setLeida(notificacion.getLeida());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setFechaLeida(notificacion.getFechaLeida());
        dto.setPrecioAnterior(notificacion.getPrecioAnterior());
        dto.setPrecioNuevo(notificacion.getPrecioNuevo());
        dto.setPorcentajeDescuentoNuevo(notificacion.getPorcentajeDescuentoNuevo());
        dto.setRebajaMayor(notificacion.getRebajaMayor());
        return dto;
    }

    private String formatearImporte(BigDecimal valor) {
        if (valor == null) {
            return "-";
        }

        return valor.stripTrailingZeros().toPlainString() + " EUR";
    }

    private String textoSeguro(String texto, String fallback) {
        if (texto == null || texto.isBlank()) {
            return fallback;
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

    private String construirUrlAbsolutaCorreo(String urlDestino) {
        if (urlDestino == null || urlDestino.isBlank()) {
            return "";
        }

        String urlLimpia = urlDestino.trim();

        if (urlLimpia.startsWith("http://") || urlLimpia.startsWith("https://")) {
            return urlLimpia;
        }

        String base = frontendUrl == null ? "" : frontendUrl.trim();

        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (urlLimpia.startsWith("/")) {
            return base + urlLimpia;
        }

        return base + "/" + urlLimpia;
    }
}
