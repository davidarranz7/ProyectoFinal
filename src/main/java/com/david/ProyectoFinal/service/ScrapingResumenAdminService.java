package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambioPrecioProductoDTO;
import com.david.ProyectoFinal.dto.ResultadoScrapingDTO;
import com.david.ProyectoFinal.model.EstadoScrapingEjecucion;
import com.david.ProyectoFinal.model.OrigenScrapingEjecucion;
import com.david.ProyectoFinal.model.Rol;
import com.david.ProyectoFinal.model.ScrapingEjecucion;
import com.david.ProyectoFinal.model.TipoCambioPrecio;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ScrapingResumenAdminService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.scraping.admin-summary.enabled:true}")
    private boolean resumenAdminEnabled;

    @Value("${app.scraping.admin-summary.fallback-email:}")
    private String fallbackEmail;

    public ScrapingResumenAdminService(EmailService emailService, UsuarioRepository usuarioRepository) {
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
    }

    public void enviarResumenSiCorresponde(ScrapingEjecucion ejecucion, ResultadoScrapingDTO resultado, Exception exception) {
        if (!resumenAdminEnabled || ejecucion == null) {
            return;
        }

        if (ejecucion.getOrigen() != OrigenScrapingEjecucion.AUTOMATICO
                && ejecucion.getOrigen() != OrigenScrapingEjecucion.REINTENTO_PENDIENTE) {
            return;
        }

        if (ejecucion.getEstado() == EstadoScrapingEjecucion.OMITIDO) {
            return;
        }

        List<String> destinatarios = resolverDestinatariosAdmin();

        if (destinatarios.isEmpty()) {
            return;
        }

        String asunto = construirAsunto(ejecucion);
        String contenidoHtml = construirContenidoHtml(ejecucion, resultado, exception);

        for (String destinatario : destinatarios) {
            emailService.enviarCorreoHtmlConResultado(destinatario, asunto, contenidoHtml);
        }
    }

    private List<String> resolverDestinatariosAdmin() {
        Set<String> destinatarios = new LinkedHashSet<>();

        for (Usuario admin : usuarioRepository.findByRol(Rol.ADMIN)) {
            if (admin.getEmail() != null && !admin.getEmail().isBlank()) {
                destinatarios.add(admin.getEmail().trim().toLowerCase(Locale.ROOT));
            }
        }

        if (fallbackEmail != null && !fallbackEmail.isBlank()) {
            destinatarios.add(fallbackEmail.trim().toLowerCase(Locale.ROOT));
        }

        return new ArrayList<>(destinatarios);
    }

    private String construirAsunto(ScrapingEjecucion ejecucion) {
        String estadoTexto = switch (ejecucion.getEstado()) {
            case EN_CURSO -> "en curso";
            case COMPLETADO -> "completado";
            case PENDIENTE -> "pendiente";
            case ERROR -> "con errores";
            case OMITIDO -> "omitido";
        };

        return "Resumen scraping " + ejecucion.getTipo().getNombreProceso() + " - " + estadoTexto;
    }

    private String construirContenidoHtml(ScrapingEjecucion ejecucion,
                                          ResultadoScrapingDTO resultado,
                                          Exception exception) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family:Arial,sans-serif; color:#1f2937; line-height:1.6;\">");
        html.append("<h2 style=\"margin:0 0 12px 0; color:#111827;\">Resumen del scraping automatico</h2>");
        html.append("<p style=\"margin:0 0 16px 0;\">");
        html.append("Proceso: <strong>").append(escaparHtml(ejecucion.getTipo().getNombreProceso())).append("</strong><br>");
        html.append("Estado: <strong>").append(escaparHtml(formatearEstado(ejecucion.getEstado()))).append("</strong><br>");
        html.append("Origen: <strong>").append(escaparHtml(formatearOrigen(ejecucion.getOrigen()))).append("</strong><br>");
        html.append("Inicio: <strong>").append(ejecucion.getFechaInicio() == null ? "-" : ejecucion.getFechaInicio().format(FORMATTER)).append("</strong><br>");
        html.append("Fin: <strong>").append(ejecucion.getFechaFin() == null ? "-" : ejecucion.getFechaFin().format(FORMATTER)).append("</strong><br>");
        html.append("Relay local: <strong>").append(Boolean.TRUE.equals(ejecucion.getRelayHabilitado()) ? "activado" : "desactivado").append("</strong>");
        html.append("</p>");

        if (resultado != null) {
            html.append("<table style=\"width:100%; border-collapse:collapse; margin:0 0 18px 0;\">");
            html.append("<tr>");
            appendCeldaMetrica(html, "Encontrados", resultado.getTotalProductosEncontrados());
            appendCeldaMetrica(html, "Nuevos", resultado.getTotalProductosNuevos());
            appendCeldaMetrica(html, "Actualizados", resultado.getTotalProductosActualizados());
            appendCeldaMetrica(html, "Cambios de precio", resultado.getTotalProductosCambioPrecio());
            html.append("</tr><tr>");
            appendCeldaMetrica(html, "Bajadas", resultado.getTotalProductosBajadaPrecio());
            appendCeldaMetrica(html, "Subidas", resultado.getTotalProductosSubidaPrecio());
            appendCeldaMetrica(html, "Rebajas mayores", resultado.getTotalProductosRebajaMayor());
            appendCeldaMetrica(html, "No disponibles", resultado.getTotalProductosDesactivados());
            html.append("</tr></table>");

            if (resultado.getMensajeEstado() != null && !resultado.getMensajeEstado().isBlank()) {
                html.append("<p style=\"margin:0 0 16px 0; padding:12px 14px; background:#f3f4f6; border-radius:12px;\">")
                        .append(escaparHtml(resultado.getMensajeEstado()))
                        .append("</p>");
            }

            List<CambioPrecioProductoDTO> cambiosDestacados = obtenerCambiosDestacados(resultado);

            if (!cambiosDestacados.isEmpty()) {
                html.append("<h3 style=\"margin:0 0 10px 0; color:#111827;\">Cambios de precio destacados</h3>");
                html.append("<table style=\"width:100%; border-collapse:collapse; margin:0 0 18px 0;\">");
                html.append("<thead><tr>")
                        .append("<th style=\"text-align:left; padding:8px; border-bottom:1px solid #e5e7eb;\">Producto</th>")
                        .append("<th style=\"text-align:left; padding:8px; border-bottom:1px solid #e5e7eb;\">Tienda</th>")
                        .append("<th style=\"text-align:left; padding:8px; border-bottom:1px solid #e5e7eb;\">Cambio</th>")
                        .append("<th style=\"text-align:left; padding:8px; border-bottom:1px solid #e5e7eb;\">Detalle</th>")
                        .append("</tr></thead><tbody>");

                for (CambioPrecioProductoDTO cambio : cambiosDestacados) {
                    html.append("<tr>");
                    html.append("<td style=\"padding:8px; border-bottom:1px solid #f3f4f6;\">")
                            .append(escaparHtml(textoSeguro(cambio.getNombreProducto())))
                            .append("</td>");
                    html.append("<td style=\"padding:8px; border-bottom:1px solid #f3f4f6;\">")
                            .append(escaparHtml(textoSeguro(cambio.getTienda())))
                            .append("</td>");
                    html.append("<td style=\"padding:8px; border-bottom:1px solid #f3f4f6;\">")
                            .append(escaparHtml(formatearCambio(cambio)))
                            .append("</td>");
                    html.append("<td style=\"padding:8px; border-bottom:1px solid #f3f4f6;\">")
                            .append(escaparHtml(formatearDetalleCambio(cambio)))
                            .append("</td>");
                    html.append("</tr>");
                }

                html.append("</tbody></table>");
            }
        }

        if (exception != null && exception.getMessage() != null && !exception.getMessage().isBlank()) {
            html.append("<p style=\"margin:0; color:#991b1b;\"><strong>Error:</strong> ")
                    .append(escaparHtml(exception.getMessage()))
                    .append("</p>");
        }

        html.append("</div>");
        return html.toString();
    }

    private void appendCeldaMetrica(StringBuilder html, String titulo, int valor) {
        html.append("<td style=\"padding:12px; border:1px solid #e5e7eb; border-radius:12px; background:#fafafa;\">")
                .append("<div style=\"font-size:12px; color:#6b7280; text-transform:uppercase; letter-spacing:0.04em;\">")
                .append(escaparHtml(titulo))
                .append("</div>")
                .append("<div style=\"font-size:22px; font-weight:700; color:#111827;\">")
                .append(valor)
                .append("</div></td>");
    }

    private List<CambioPrecioProductoDTO> obtenerCambiosDestacados(ResultadoScrapingDTO resultado) {
        if (resultado == null || resultado.getCambiosPrecio() == null || resultado.getCambiosPrecio().isEmpty()) {
            return List.of();
        }

        return resultado.getCambiosPrecio()
                .stream()
                .sorted(
                        Comparator.comparing(this::prioridadTipoCambio)
                                .thenComparing(this::magnitudVariacion, Comparator.reverseOrder())
                )
                .limit(10)
                .toList();
    }

    private int prioridadTipoCambio(CambioPrecioProductoDTO cambio) {
        if (cambio == null || cambio.getTipoCambio() == null) {
            return 3;
        }

        if (cambio.getTipoCambio() == TipoCambioPrecio.BAJADA) {
            return Boolean.TRUE.equals(cambio.getRebajaMayor()) ? 0 : 1;
        }

        if (cambio.getTipoCambio() == TipoCambioPrecio.SUBIDA) {
            return 2;
        }

        return 3;
    }

    private BigDecimal magnitudVariacion(CambioPrecioProductoDTO cambio) {
        if (cambio == null || cambio.getPorcentajeVariacionPrecio() == null) {
            return BigDecimal.ZERO;
        }

        return cambio.getPorcentajeVariacionPrecio().abs();
    }

    private String formatearEstado(EstadoScrapingEjecucion estado) {
        return switch (estado) {
            case EN_CURSO -> "En curso";
            case COMPLETADO -> "Completado";
            case PENDIENTE -> "Pendiente";
            case ERROR -> "Error";
            case OMITIDO -> "Omitido";
        };
    }

    private String formatearOrigen(OrigenScrapingEjecucion origen) {
        return switch (origen) {
            case MANUAL -> "Manual";
            case AUTOMATICO -> "Automatico";
            case REINTENTO_PENDIENTE -> "Reintento pendiente";
        };
    }

    private String formatearCambio(CambioPrecioProductoDTO cambio) {
        String detalle = formatearImporte(cambio.getPrecioAnterior()) + " -> " + formatearImporte(cambio.getPrecioNuevo());

        if (cambio.getPorcentajeVariacionPrecio() == null) {
            return detalle;
        }

        String signo = cambio.getPorcentajeVariacionPrecio().compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return detalle + " (" + signo + cambio.getPorcentajeVariacionPrecio().stripTrailingZeros().toPlainString() + "%)";
    }

    private String formatearDetalleCambio(CambioPrecioProductoDTO cambio) {
        List<String> partes = new ArrayList<>();

        if (cambio.getPorcentajeDescuentoAnterior() != null || cambio.getPorcentajeDescuentoNuevo() != null) {
            partes.add("Descuento " + valorSeguro(cambio.getPorcentajeDescuentoAnterior()) + "% -> " + valorSeguro(cambio.getPorcentajeDescuentoNuevo()) + "%");
        }

        if (Boolean.TRUE.equals(cambio.getRebajaMayor())) {
            partes.add("rebaja mayor");
        }

        if (cambio.getUrlProducto() != null && !cambio.getUrlProducto().isBlank()) {
            partes.add(cambio.getUrlProducto().trim());
        }

        return partes.isEmpty() ? "Cambio detectado" : String.join(" | ", partes);
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private String formatearImporte(BigDecimal valor) {
        if (valor == null) {
            return "-";
        }

        return valor.stripTrailingZeros().toPlainString() + " EUR";
    }

    private String textoSeguro(String texto) {
        if (texto == null || texto.isBlank()) {
            return "-";
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
