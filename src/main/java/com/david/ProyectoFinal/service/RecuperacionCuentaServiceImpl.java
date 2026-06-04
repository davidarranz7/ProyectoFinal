package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambiarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.CorreoOperacionResponseDTO;
import com.david.ProyectoFinal.dto.RecuperarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarUsuarioRequestDTO;
import com.david.ProyectoFinal.model.PasswordResetToken;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.PasswordResetTokenRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionCuentaServiceImpl implements RecuperacionCuentaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public RecuperacionCuentaServiceImpl(UsuarioRepository usuarioRepository,
                                         PasswordResetTokenRepository passwordResetTokenRepository,
                                         EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public CorreoOperacionResponseDTO solicitarRecuperacionPassword(RecuperarPasswordRequestDTO request) {
        if (request.getIdentificador() == null || request.getIdentificador().trim().isBlank()) {
            throw new RuntimeException("Debes introducir tu email o nombre de usuario");
        }

        String identificador = request.getIdentificador().trim();
        Optional<Usuario> usuarioOptional = identificador.contains("@")
                ? usuarioRepository.findByEmailIgnoreCase(identificador)
                : usuarioRepository.findByNombreIgnoreCase(identificador);

        if (usuarioOptional.isEmpty()) {
            throw new RuntimeException("No existe ninguna cuenta con ese email o nombre de usuario");
        }

        Usuario usuario = usuarioOptional.get();

        passwordResetTokenRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(5));
        passwordResetToken.setUsado(false);

        passwordResetTokenRepository.save(passwordResetToken);

        String enlace = frontendUrl + "/cambiar-password.html?token=" + token;
        String contenidoHtml = construirRecuperacionPasswordHtml(usuario, enlace);

        EmailDispatchResult resultadoCorreo;

        try {
            resultadoCorreo = emailService.enviarCorreoHtmlConResultado(
                    usuario.getEmail(),
                    "Recuperacion de contrasena",
                    contenidoHtml
            );
        } catch (RuntimeException e) {
            resultadoCorreo = EmailDispatchResult.pendiente(
                    "Tu solicitud ha quedado registrada. El correo se enviara en cuanto vuelva a estar disponible el servicio."
            );
        }

        if (resultadoCorreo.isPendiente()) {
            return new CorreoOperacionResponseDTO(
                    "Tu solicitud ha quedado registrada. El correo se enviara en cuanto vuelva a estar disponible el servicio.",
                    true
            );
        }

        return new CorreoOperacionResponseDTO(
                "Si existe una cuenta con esos datos, recibiras un correo con instrucciones.",
                false
        );
    }

    @Override
    @Transactional
    public void cambiarPassword(CambiarPasswordRequestDTO request) {
        if (request.getToken() == null || request.getToken().trim().isBlank()) {
            throw new RuntimeException("El token es obligatorio");
        }

        if (request.getNuevaPassword() == null || request.getRepetirPassword() == null) {
            throw new RuntimeException("Todos los campos son obligatorios");
        }

        String token = request.getToken().trim();
        String nuevaPassword = request.getNuevaPassword().trim();
        String repetirPassword = request.getRepetirPassword().trim();

        if (nuevaPassword.isBlank() || repetirPassword.isBlank()) {
            throw new RuntimeException("La contrasena no puede estar vacia");
        }

        if (!nuevaPassword.equals(repetirPassword)) {
            throw new RuntimeException("Las contrasenas no coinciden");
        }

        if (nuevaPassword.length() < 4) {
            throw new RuntimeException("La nueva contrasena debe tener al menos 4 caracteres");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace de recuperacion no es valido"));

        if (passwordResetToken.isUsado()) {
            throw new RuntimeException("Este enlace ya ha sido utilizado");
        }

        if (passwordResetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace de recuperacion ha caducado");
        }

        Usuario usuario = passwordResetToken.getUsuario();
        usuario.setPassword(nuevaPassword);
        usuarioRepository.save(usuario);

        passwordResetToken.setUsado(true);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public CorreoOperacionResponseDTO solicitarRecuperacionUsuario(RecuperarUsuarioRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isBlank()) {
            throw new RuntimeException("Debes introducir tu email");
        }

        String email = request.getEmail().trim();

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("El formato del email no es valido");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("No existe ninguna cuenta con ese email"));

        String contenidoHtml = construirRecuperacionUsuarioHtml(usuario);

        EmailDispatchResult resultadoCorreo;

        try {
            resultadoCorreo = emailService.enviarCorreoHtmlConResultado(
                    usuario.getEmail(),
                    "Recuperacion de usuario",
                    contenidoHtml
            );
        } catch (RuntimeException e) {
            resultadoCorreo = EmailDispatchResult.pendiente(
                    "Tu solicitud ha quedado registrada. El correo se enviara en cuanto vuelva a estar disponible el servicio."
            );
        }

        if (resultadoCorreo.isPendiente()) {
            return new CorreoOperacionResponseDTO(
                    "Tu solicitud ha quedado registrada. El correo se enviara en cuanto vuelva a estar disponible el servicio.",
                    true
            );
        }

        return new CorreoOperacionResponseDTO(
                "Te hemos enviado un correo con tu nombre de usuario.",
                false
        );
    }

    private String construirRecuperacionPasswordHtml(Usuario usuario, String enlace) {
        String plantilla = leerPlantillaHtml("templates/recuperacionPassword.html");

        return plantilla
                .replace("{{NOMBRE_USUARIO}}", usuario.getNombre())
                .replace("{{ENLACE_RECUPERACION}}", enlace)
                .replace("{{MINUTOS_EXPIRACION}}", "5");
    }

    private String construirRecuperacionUsuarioHtml(Usuario usuario) {
        String plantilla = leerPlantillaHtml("templates/recuperacionUsuario.html");

        return plantilla
                .replace("{{NOMBRE_USUARIO}}", usuario.getNombre());
    }

    private String leerPlantillaHtml(String ruta) {
        try (InputStream inputStream = new ClassPathResource(ruta).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla HTML: " + ruta, e);
        }
    }
}
