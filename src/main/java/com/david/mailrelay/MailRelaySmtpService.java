package com.david.mailrelay;

import com.david.ProyectoFinal.dto.MailRelayRequestDTO;
import com.david.ProyectoFinal.model.TipoCorreoPendiente;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class MailRelaySmtpService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public MailRelaySmtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(MailRelayRequestDTO request) {
        TipoCorreoPendiente tipo = TipoCorreoPendiente.valueOf(request.getTipo());

        try {
            if (tipo == TipoCorreoPendiente.TEXTO) {
                SimpleMailMessage mensaje = new SimpleMailMessage();
                mensaje.setFrom(remitente);
                mensaje.setTo(request.getDestinatario());
                mensaje.setSubject(request.getAsunto());
                mensaje.setText(request.getContenido());
                mailSender.send(mensaje);
                return;
            }

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(request.getDestinatario());
            helper.setSubject(request.getAsunto());
            helper.setText(request.getContenido(), true);

            if (tipo == TipoCorreoPendiente.HTML_INLINE_IMAGE && request.getImagenInlineBase64() != null) {
                byte[] imagenBytes = Base64.getDecoder().decode(request.getImagenInlineBase64());
                String mimeType = request.getMimeType() == null || request.getMimeType().isBlank()
                        ? "image/png"
                        : request.getMimeType();

                helper.addInline(
                        request.getContentId(),
                        new ByteArrayResource(imagenBytes),
                        mimeType
                );
            }

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo enviar el correo desde el relay local", e);
        }
    }
}
