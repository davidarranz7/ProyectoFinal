package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.MailRelayRequestDTO;
import com.david.ProyectoFinal.model.CorreoPendiente;
import com.david.ProyectoFinal.model.EstadoCorreoPendiente;
import com.david.ProyectoFinal.model.TipoCorreoPendiente;
import com.david.ProyectoFinal.repository.CorreoPendienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CorreoPendienteRepository correoPendienteRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.mail.relay.sender.enabled:false}")
    private boolean relaySenderEnabled;

    @Value("${app.mail.relay.receiver-url:http://127.0.0.1:8095/internal/mail-relay/send}")
    private String relayReceiverUrl;

    @Value("${app.mail.relay.token:}")
    private String relayToken;

    @Value("${app.mail.relay.connect-timeout-ms:2500}")
    private long relayConnectTimeoutMs;

    @Value("${app.mail.relay.read-timeout-ms:8000}")
    private long relayReadTimeoutMs;

    @Override
    public void enviarCorreoSimple(String destinatario, String asunto, String contenido) {
        enviarCorreoSimpleConResultado(destinatario, asunto, contenido);
    }

    @Override
    public void enviarCorreoHtml(String destinatario, String asunto, String contenidoHtml) {
        enviarCorreoHtmlConResultado(destinatario, asunto, contenidoHtml);
    }

    @Override
    public void enviarCorreoHtmlConImagenInline(String destinatario,
                                                String asunto,
                                                String contenidoHtml,
                                                byte[] imagenBytes,
                                                String contentId,
                                                String nombreArchivo) {
        enviarCorreoHtmlConImagenInlineConResultado(
                destinatario,
                asunto,
                contenidoHtml,
                imagenBytes,
                contentId,
                nombreArchivo
        );
    }

    @Override
    public EmailDispatchResult enviarCorreoSimpleConResultado(String destinatario, String asunto, String contenido) {
        MailRelayRequestDTO solicitud = new MailRelayRequestDTO();
        solicitud.setTipo(TipoCorreoPendiente.TEXTO.name());
        solicitud.setDestinatario(destinatario);
        solicitud.setAsunto(asunto);
        solicitud.setContenido(contenido);

        return enviarSolicitudCorreo(solicitud);
    }

    @Override
    public EmailDispatchResult enviarCorreoHtmlConResultado(String destinatario, String asunto, String contenidoHtml) {
        MailRelayRequestDTO solicitud = new MailRelayRequestDTO();
        solicitud.setTipo(TipoCorreoPendiente.HTML.name());
        solicitud.setDestinatario(destinatario);
        solicitud.setAsunto(asunto);
        solicitud.setContenido(contenidoHtml);

        return enviarSolicitudCorreo(solicitud);
    }

    @Override
    public EmailDispatchResult enviarCorreoHtmlConImagenInlineConResultado(String destinatario,
                                                                           String asunto,
                                                                           String contenidoHtml,
                                                                           byte[] imagenBytes,
                                                                           String contentId,
                                                                           String nombreArchivo) {
        MailRelayRequestDTO solicitud = new MailRelayRequestDTO();
        solicitud.setTipo(TipoCorreoPendiente.HTML_INLINE_IMAGE.name());
        solicitud.setDestinatario(destinatario);
        solicitud.setAsunto(asunto);
        solicitud.setContenido(contenidoHtml);
        solicitud.setContentId(contentId);
        solicitud.setNombreArchivo(nombreArchivo);
        solicitud.setMimeType("image/png");

        if (imagenBytes != null && imagenBytes.length > 0) {
            solicitud.setImagenInlineBase64(Base64.getEncoder().encodeToString(imagenBytes));
        }

        return enviarSolicitudCorreo(solicitud);
    }

    @Scheduled(fixedDelayString = "${app.mail.relay.retry-interval-ms:60000}")
    public void reintentarCorreosPendientes() {
        if (!relaySenderEnabled) {
            return;
        }

        List<CorreoPendiente> pendientes = correoPendienteRepository.findTop10ByEstadoOrderByFechaCreacionAsc(
                EstadoCorreoPendiente.PENDIENTE
        );

        for (CorreoPendiente correoPendiente : pendientes) {
            try {
                enviarViaRelay(convertirAPayload(correoPendiente));
                marcarCorreoComoEnviado(correoPendiente);
            } catch (Exception e) {
                registrarIntentoFallido(correoPendiente, e);
            }
        }
    }

    private EmailDispatchResult enviarSolicitudCorreo(MailRelayRequestDTO solicitud) {
        if (!relaySenderEnabled) {
            enviarPorSmtp(solicitud);
            return EmailDispatchResult.enviado("Correo enviado correctamente.");
        }

        try {
            enviarViaRelay(solicitud);
            return EmailDispatchResult.enviado("Correo enviado correctamente.");
        } catch (Exception e) {
            guardarCorreoPendiente(solicitud, e);
            return EmailDispatchResult.pendiente(
                    "El correo queda pendiente. Se enviara automaticamente en cuanto tu equipo local vuelva a estar disponible."
            );
        }
    }

    private void enviarViaRelay(MailRelayRequestDTO solicitud) throws IOException, InterruptedException {
        validarConfiguracionRelay();

        String payload = objectMapper.writeValueAsString(solicitud);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(relayReceiverUrl))
                .timeout(Duration.ofMillis(relayReadTimeoutMs))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Relay-Token", relayToken)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = crearHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }

        throw new IOException("Relay local no disponible. Codigo HTTP: " + response.statusCode());
    }

    private void validarConfiguracionRelay() {
        if (relayReceiverUrl == null || relayReceiverUrl.isBlank()) {
            throw new RuntimeException("Falta configurar app.mail.relay.receiver-url");
        }

        if (relayToken == null || relayToken.isBlank()) {
            throw new RuntimeException("Falta configurar app.mail.relay.token");
        }
    }

    private void enviarPorSmtp(MailRelayRequestDTO solicitud) {
        TipoCorreoPendiente tipo = TipoCorreoPendiente.valueOf(solicitud.getTipo());

        try {
            if (tipo == TipoCorreoPendiente.TEXTO) {
                SimpleMailMessage mensaje = new SimpleMailMessage();
                mensaje.setFrom(remitente);
                mensaje.setTo(solicitud.getDestinatario());
                mensaje.setSubject(solicitud.getAsunto());
                mensaje.setText(solicitud.getContenido());
                mailSender.send(mensaje);
                return;
            }

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(solicitud.getDestinatario());
            helper.setSubject(solicitud.getAsunto());
            helper.setText(solicitud.getContenido(), true);

            if (tipo == TipoCorreoPendiente.HTML_INLINE_IMAGE && solicitud.getImagenInlineBase64() != null) {
                byte[] imagenBytes = Base64.getDecoder().decode(solicitud.getImagenInlineBase64());
                String mimeType = solicitud.getMimeType() == null || solicitud.getMimeType().isBlank()
                        ? "image/png"
                        : solicitud.getMimeType();

                helper.addInline(
                        solicitud.getContentId(),
                        new ByteArrayResource(imagenBytes),
                        mimeType
                );
            }

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }

    private void guardarCorreoPendiente(MailRelayRequestDTO solicitud, Exception e) {
        CorreoPendiente correoPendiente = new CorreoPendiente();
        correoPendiente.setTipo(TipoCorreoPendiente.valueOf(solicitud.getTipo()));
        correoPendiente.setEstado(EstadoCorreoPendiente.PENDIENTE);
        correoPendiente.setDestinatario(solicitud.getDestinatario());
        correoPendiente.setAsunto(solicitud.getAsunto());
        correoPendiente.setContenido(solicitud.getContenido());
        correoPendiente.setImagenInlineBase64(solicitud.getImagenInlineBase64());
        correoPendiente.setContentId(solicitud.getContentId());
        correoPendiente.setNombreArchivo(solicitud.getNombreArchivo());
        correoPendiente.setMimeType(solicitud.getMimeType());
        correoPendiente.setIntentos(1);
        correoPendiente.setUltimoError(limpiarMensajeError(e));
        correoPendiente.setFechaCreacion(LocalDateTime.now());
        correoPendiente.setFechaUltimoIntento(LocalDateTime.now());

        correoPendienteRepository.save(correoPendiente);
    }

    private void marcarCorreoComoEnviado(CorreoPendiente correoPendiente) {
        correoPendiente.setEstado(EstadoCorreoPendiente.ENVIADO);
        correoPendiente.setFechaEnviado(LocalDateTime.now());
        correoPendiente.setFechaUltimoIntento(LocalDateTime.now());
        correoPendiente.setUltimoError(null);
        correoPendienteRepository.save(correoPendiente);
    }

    private void registrarIntentoFallido(CorreoPendiente correoPendiente, Exception e) {
        int intentosActuales = correoPendiente.getIntentos() == null ? 0 : correoPendiente.getIntentos();
        correoPendiente.setIntentos(intentosActuales + 1);
        correoPendiente.setFechaUltimoIntento(LocalDateTime.now());
        correoPendiente.setUltimoError(limpiarMensajeError(e));
        correoPendienteRepository.save(correoPendiente);
    }

    private MailRelayRequestDTO convertirAPayload(CorreoPendiente correoPendiente) {
        MailRelayRequestDTO solicitud = new MailRelayRequestDTO();
        solicitud.setTipo(correoPendiente.getTipo().name());
        solicitud.setDestinatario(correoPendiente.getDestinatario());
        solicitud.setAsunto(correoPendiente.getAsunto());
        solicitud.setContenido(correoPendiente.getContenido());
        solicitud.setImagenInlineBase64(correoPendiente.getImagenInlineBase64());
        solicitud.setContentId(correoPendiente.getContentId());
        solicitud.setNombreArchivo(correoPendiente.getNombreArchivo());
        solicitud.setMimeType(correoPendiente.getMimeType());
        return solicitud;
    }

    private String limpiarMensajeError(Exception e) {
        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            return "No se pudo contactar con el relay local.";
        }

        return e.getMessage();
    }

    private HttpClient crearHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(relayConnectTimeoutMs))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}
