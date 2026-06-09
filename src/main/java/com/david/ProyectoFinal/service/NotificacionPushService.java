package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.EstadoSuscripcionPushDTO;
import com.david.ProyectoFinal.dto.SuscripcionPushRequestDTO;
import com.david.ProyectoFinal.model.NotificacionUsuario;
import com.david.ProyectoFinal.model.SuscripcionPushUsuario;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.SuscripcionPushUsuarioRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Urgency;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionPushService {

    private final SuscripcionPushUsuarioRepository suscripcionPushUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.user-notifications.push.enabled:false}")
    private boolean pushEnabled;

    @Value("${app.user-notifications.push.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${app.user-notifications.push.vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${app.user-notifications.push.subject:}")
    private String vapidSubject;

    @Value("${app.user-notifications.push.ttl-seconds:3600}")
    private int ttlSeconds;

    public NotificacionPushService(SuscripcionPushUsuarioRepository suscripcionPushUsuarioRepository,
                                   UsuarioRepository usuarioRepository) {
        this.suscripcionPushUsuarioRepository = suscripcionPushUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        registrarProveedorCriptografia();
    }

    public EstadoSuscripcionPushDTO obtenerEstadoUsuario(Long usuarioId) {
        EstadoSuscripcionPushDTO dto = new EstadoSuscripcionPushDTO();
        dto.setHabilitado(pushEnabled);
        dto.setConfigurado(pushConfigurado());
        dto.setSuscrito(
                usuarioId != null
                        && suscripcionPushUsuarioRepository.countByUsuarioIdAndActivaTrue(usuarioId) > 0
        );
        dto.setClavePublica(pushConfigurado() ? vapidPublicKey.trim() : null);

        if (!pushEnabled) {
            dto.setMensaje("Las notificaciones push estan desactivadas en el servidor.");
        } else if (!pushConfigurado()) {
            dto.setMensaje("Faltan las claves VAPID para enviar notificaciones push.");
        } else if (Boolean.TRUE.equals(dto.getSuscrito())) {
            dto.setMensaje("Las notificaciones push ya estan activadas en este usuario.");
        } else {
            dto.setMensaje("Puedes activar notificaciones reales en este dispositivo.");
        }

        return dto;
    }

    @Transactional
    public EstadoSuscripcionPushDTO guardarSuscripcion(Long usuarioId, SuscripcionPushRequestDTO dto) {
        if (!pushEnabled) {
            throw new RuntimeException("Las notificaciones push no estan habilitadas");
        }

        if (!pushConfigurado()) {
            throw new RuntimeException("Faltan las claves VAPID del servidor");
        }

        validarSuscripcion(dto);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        SuscripcionPushUsuario suscripcion = suscripcionPushUsuarioRepository.findByEndpoint(dto.getEndpoint().trim())
                .orElseGet(SuscripcionPushUsuario::new);

        if (suscripcion.getFechaCreacion() == null) {
            suscripcion.setFechaCreacion(ahora);
        }

        suscripcion.setUsuario(usuario);
        suscripcion.setEndpoint(dto.getEndpoint().trim());
        suscripcion.setP256dh(dto.getKeys().getP256dh().trim());
        suscripcion.setAuth(dto.getKeys().getAuth().trim());
        suscripcion.setExpirationTime(dto.getExpirationTime());
        suscripcion.setActiva(true);
        suscripcion.setFechaActualizacion(ahora);
        suscripcion.setFechaUltimoError(null);
        suscripcion.setUltimoError(null);
        suscripcionPushUsuarioRepository.save(suscripcion);

        return obtenerEstadoUsuario(usuarioId);
    }

    @Transactional
    public EstadoSuscripcionPushDTO eliminarSuscripcion(Long usuarioId, String endpoint) {
        List<SuscripcionPushUsuario> suscripciones = suscripcionPushUsuarioRepository.findByUsuarioIdAndActivaTrue(usuarioId);

        for (SuscripcionPushUsuario suscripcion : suscripciones) {
            if (endpoint != null && !endpoint.isBlank() && !endpoint.trim().equals(suscripcion.getEndpoint())) {
                continue;
            }

            suscripcion.setActiva(false);
            suscripcion.setFechaActualizacion(LocalDateTime.now());
            suscripcionPushUsuarioRepository.save(suscripcion);
        }

        return obtenerEstadoUsuario(usuarioId);
    }

    public void enviarNotificacion(Usuario usuario, NotificacionUsuario notificacion) {
        if (usuario == null || usuario.getId() == null || notificacion == null || !pushConfigurado()) {
            return;
        }

        List<SuscripcionPushUsuario> suscripciones = suscripcionPushUsuarioRepository.findByUsuarioIdAndActivaTrue(usuario.getId());

        if (suscripciones.isEmpty()) {
            return;
        }

        for (SuscripcionPushUsuario suscripcion : suscripciones) {
            enviarNotificacionASuscripcion(suscripcion, notificacion);
        }
    }

    private void enviarNotificacionASuscripcion(SuscripcionPushUsuario suscripcion, NotificacionUsuario notificacion) {
        try {
            PushService pushService = new PushService(
                    vapidPublicKey.trim(),
                    vapidPrivateKey.trim(),
                    vapidSubject.trim()
            );

            Notification notification = Notification.builder()
                    .endpoint(suscripcion.getEndpoint())
                    .userPublicKey(suscripcion.getP256dh())
                    .userAuth(suscripcion.getAuth())
                    .payload(construirPayload(notificacion))
                    .ttl(Math.max(60, ttlSeconds))
                    .urgency(Urgency.HIGH)
                    .topic(construirTopic(notificacion))
                    .build();

            HttpResponse response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                suscripcion.setFechaUltimoEnvioExitoso(LocalDateTime.now());
                suscripcion.setFechaUltimoError(null);
                suscripcion.setUltimoError(null);
                suscripcion.setFechaActualizacion(LocalDateTime.now());
                suscripcionPushUsuarioRepository.save(suscripcion);
                return;
            }

            if (status == 404 || status == 410) {
                desactivarSuscripcion(suscripcion, "Suscripcion push caducada o invalida (" + status + ")");
                return;
            }

            registrarErrorSuscripcion(suscripcion, "Error HTTP push " + status);
        } catch (GeneralSecurityException e) {
            registrarErrorSuscripcion(suscripcion, "Error de seguridad push: " + e.getMessage());
        } catch (Exception e) {
            registrarErrorSuscripcion(suscripcion, "No se pudo enviar push: " + e.getMessage());
        }
    }

    private String construirPayload(NotificacionUsuario notificacion) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", textoSeguro(notificacion.getTitulo(), "Nueva notificacion"));
        payload.put("body", textoSeguro(notificacion.getMensaje(), "Tienes una nueva notificacion"));
        payload.put("url", textoSeguro(notificacion.getUrlDestino(), "notificaciones.html"));
        payload.put("tag", construirTopic(notificacion));
        payload.put("icon", "/icon.svg");
        payload.put("badge", "/icon.svg");
        payload.put("rebajaMayor", Boolean.TRUE.equals(notificacion.getRebajaMayor()));
        payload.put("productoId", notificacion.getProducto() == null ? null : notificacion.getProducto().getId());
        return objectMapper.writeValueAsString(payload);
    }

    private String construirTopic(NotificacionUsuario notificacion) {
        if (notificacion == null || notificacion.getId() == null) {
            return "moda-precio";
        }

        return "moda-precio-" + notificacion.getId();
    }

    private void validarSuscripcion(SuscripcionPushRequestDTO dto) {
        if (dto == null || dto.getEndpoint() == null || dto.getEndpoint().isBlank()) {
            throw new RuntimeException("La suscripcion push no tiene endpoint");
        }

        if (dto.getKeys() == null
                || dto.getKeys().getP256dh() == null
                || dto.getKeys().getP256dh().isBlank()
                || dto.getKeys().getAuth() == null
                || dto.getKeys().getAuth().isBlank()) {
            throw new RuntimeException("Faltan las claves de la suscripcion push");
        }
    }

    private boolean pushConfigurado() {
        return pushEnabled
                && vapidPublicKey != null
                && !vapidPublicKey.isBlank()
                && vapidPrivateKey != null
                && !vapidPrivateKey.isBlank()
                && vapidSubject != null
                && !vapidSubject.isBlank();
    }

    private void registrarProveedorCriptografia() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private void desactivarSuscripcion(SuscripcionPushUsuario suscripcion, String motivo) {
        suscripcion.setActiva(false);
        suscripcion.setFechaActualizacion(LocalDateTime.now());
        suscripcion.setFechaUltimoError(LocalDateTime.now());
        suscripcion.setUltimoError(motivo);
        suscripcionPushUsuarioRepository.save(suscripcion);
    }

    private void registrarErrorSuscripcion(SuscripcionPushUsuario suscripcion, String mensaje) {
        suscripcion.setFechaUltimoError(LocalDateTime.now());
        suscripcion.setUltimoError(truncar(mensaje, 500));
        suscripcion.setFechaActualizacion(LocalDateTime.now());
        suscripcionPushUsuarioRepository.save(suscripcion);
    }

    private String truncar(String texto, int longitudMaxima) {
        if (texto == null || texto.length() <= longitudMaxima) {
            return texto;
        }

        return texto.substring(0, longitudMaxima);
    }

    private String textoSeguro(String texto, String fallback) {
        if (texto == null || texto.isBlank()) {
            return fallback;
        }

        return texto.trim();
    }
}
