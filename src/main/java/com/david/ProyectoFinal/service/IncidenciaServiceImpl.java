package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CrearIncidenciaRequestDTO;
import com.david.ProyectoFinal.dto.IncidenciaResponseDTO;
import com.david.ProyectoFinal.model.EstadoIncidencia;
import com.david.ProyectoFinal.model.Incidencia;
import com.david.ProyectoFinal.model.MensajeIncidencia;
import com.david.ProyectoFinal.model.OrigenMensajeIncidencia;
import com.david.ProyectoFinal.model.RemitenteMensajeIncidencia;
import com.david.ProyectoFinal.model.TipoIncidencia;
import com.david.ProyectoFinal.repository.IncidenciaRepository;
import com.david.ProyectoFinal.repository.MensajeIncidenciaRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final MensajeIncidenciaRepository mensajeIncidenciaRepository;
    private final EmailService emailService;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository,
                                 MensajeIncidenciaRepository mensajeIncidenciaRepository,
                                 EmailService emailService) {
        this.incidenciaRepository = incidenciaRepository;
        this.mensajeIncidenciaRepository = mensajeIncidenciaRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public IncidenciaResponseDTO crearIncidencia(CrearIncidenciaRequestDTO request) {
        validarCrearIncidencia(request);

        Incidencia incidencia = new Incidencia();
        incidencia.setCodigoSeguimiento(generarCodigoSeguimiento());
        incidencia.setNombreContacto(request.getNombreContacto().trim());
        incidencia.setEmailContacto(request.getEmailContacto().trim());
        incidencia.setUsuarioRelacionado(limpiarTextoOpcional(request.getUsuarioRelacionado()));
        incidencia.setNumeroPedido(request.getNumeroPedido());
        incidencia.setTipoIncidencia(request.getTipoIncidencia());
        incidencia.setEstadoIncidencia(EstadoIncidencia.PENDIENTE);
        incidencia.setAsunto(request.getAsunto().trim());
        incidencia.setMensajeInicial(request.getMensaje().trim());

        Incidencia incidenciaGuardada = incidenciaRepository.save(incidencia);

        MensajeIncidencia mensajeInicial = new MensajeIncidencia();
        mensajeInicial.setIncidencia(incidenciaGuardada);
        mensajeInicial.setRemitente(RemitenteMensajeIncidencia.USUARIO);
        mensajeInicial.setOrigen(OrigenMensajeIncidencia.WEB);
        mensajeInicial.setEmailRemitente(incidenciaGuardada.getEmailContacto());
        mensajeInicial.setContenido(incidenciaGuardada.getMensajeInicial());

        mensajeIncidenciaRepository.save(mensajeInicial);

        enviarCorreoConfirmacionIncidencia(incidenciaGuardada);

        return convertirAResponseDTO(incidenciaGuardada);
    }

    private void validarCrearIncidencia(CrearIncidenciaRequestDTO request) {
        if (request.getNombreContacto() == null || request.getNombreContacto().trim().isBlank()) {
            throw new RuntimeException("El nombre de contacto es obligatorio");
        }

        if (request.getEmailContacto() == null || request.getEmailContacto().trim().isBlank()) {
            throw new RuntimeException("El email de contacto es obligatorio");
        }

        if (!request.getEmailContacto().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("El formato del email no es válido");
        }

        if (request.getTipoIncidencia() == null) {
            throw new RuntimeException("El tipo de incidencia es obligatorio");
        }

        if (request.getAsunto() == null || request.getAsunto().trim().isBlank()) {
            throw new RuntimeException("El asunto es obligatorio");
        }

        if (request.getMensaje() == null || request.getMensaje().trim().isBlank()) {
            throw new RuntimeException("El mensaje es obligatorio");
        }

        if (request.getMensaje().trim().length() < 10) {
            throw new RuntimeException("El mensaje debe tener al menos 10 caracteres");
        }
    }

    private void enviarCorreoConfirmacionIncidencia(Incidencia incidencia) {
        try {
            String asunto = "[" + incidencia.getCodigoSeguimiento() + "] Incidencia recibida";
            String contenidoHtml = construirIncidenciaCreadaHtml(incidencia);

            emailService.enviarCorreoHtml(
                    incidencia.getEmailContacto(),
                    asunto,
                    contenidoHtml
            );
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO DE CONFIRMACIÓN DE INCIDENCIA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String construirIncidenciaCreadaHtml(Incidencia incidencia) {
        String plantilla = leerPlantillaHtml("templates/incidenciaCreada.html");

        return plantilla
                .replace("{{NOMBRE_CONTACTO}}", incidencia.getNombreContacto())
                .replace("{{CODIGO_SEGUIMIENTO}}", incidencia.getCodigoSeguimiento())
                .replace("{{ESTADO_INCIDENCIA}}", formatearEstadoIncidencia(incidencia.getEstadoIncidencia()))
                .replace("{{TIPO_INCIDENCIA}}", formatearTipoIncidencia(incidencia.getTipoIncidencia()))
                .replace("{{ASUNTO_INCIDENCIA}}", incidencia.getAsunto());
    }

    private String generarCodigoSeguimiento() {
        String codigoAleatorio = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "INC-" + codigoAleatorio;
    }

    private String limpiarTextoOpcional(String texto) {
        if (texto == null || texto.trim().isBlank()) {
            return null;
        }

        return texto.trim();
    }

    private IncidenciaResponseDTO convertirAResponseDTO(Incidencia incidencia) {
        return new IncidenciaResponseDTO(
                incidencia.getId(),
                incidencia.getCodigoSeguimiento(),
                incidencia.getNombreContacto(),
                incidencia.getEmailContacto(),
                incidencia.getTipoIncidencia(),
                incidencia.getEstadoIncidencia(),
                incidencia.getAsunto(),
                incidencia.getFechaCreacion()
        );
    }

    private String leerPlantillaHtml(String ruta) {
        try (InputStream inputStream = new ClassPathResource(ruta).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla HTML: " + ruta, e);
        }
    }

    private String formatearTipoIncidencia(TipoIncidencia tipoIncidencia) {
        return switch (tipoIncidencia) {
            case PROBLEMA_ACCESO -> "Problema de acceso";
            case NO_RECUERDO_DATOS -> "No recuerdo mis datos";
            case SIN_ACCESO_EMAIL -> "Sin acceso al email";
            case PROBLEMA_PEDIDO -> "Problema con un pedido";
            case PROBLEMA_PAGO -> "Problema con el pago";
            case PRODUCTO_DEFECTUOSO -> "Producto defectuoso";
            case ERROR_WEB -> "Error en la web";
            case OTRO -> "Otro";
        };
    }

    private String formatearEstadoIncidencia(EstadoIncidencia estadoIncidencia) {
        return switch (estadoIncidencia) {
            case PENDIENTE -> "Pendiente";
            case EN_REVISION -> "En revisión";
            case ESPERANDO_RESPUESTA_USUARIO -> "Esperando respuesta del usuario";
            case RESPONDIDA_POR_USUARIO -> "Respondida por el usuario";
            case RESUELTA -> "Resuelta";
            case CERRADA -> "Cerrada";
        };
    }
}