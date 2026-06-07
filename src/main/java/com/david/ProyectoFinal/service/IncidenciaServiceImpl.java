package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CrearIncidenciaRequestDTO;
import com.david.ProyectoFinal.dto.IncidenciaResponseDTO;
import com.david.ProyectoFinal.dto.MensajeIncidenciaResponseDTO;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        EmailDispatchResult resultadoCorreo = enviarCorreoConfirmacionIncidencia(incidenciaGuardada);
        return convertirAResponseDTO(incidenciaGuardada, resultadoCorreo);
    }

    @Override
    public List<IncidenciaResponseDTO> obtenerTodasLasIncidencias() {
        return incidenciaRepository.findAllByOrderByFechaUltimaActualizacionDesc()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    public List<IncidenciaResponseDTO> obtenerIncidenciasPorEstado(EstadoIncidencia estadoIncidencia) {
        if (estadoIncidencia == null) {
            throw new RuntimeException("El estado de incidencia es obligatorio");
        }

        return incidenciaRepository.findByEstadoIncidenciaOrderByFechaUltimaActualizacionDesc(estadoIncidencia)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Override
    public IncidenciaResponseDTO obtenerIncidenciaPorId(Long incidenciaId) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

        return convertirAResponseDTO(incidencia);
    }

    @Override
    public List<MensajeIncidenciaResponseDTO> obtenerMensajesDeIncidencia(Long incidenciaId) {
        if (!incidenciaRepository.existsById(incidenciaId)) {
            throw new RuntimeException("Incidencia no encontrada");
        }

        return mensajeIncidenciaRepository.findByIncidenciaIdOrderByFechaMensajeAsc(incidenciaId)
                .stream()
                .map(this::convertirMensajeAResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public IncidenciaResponseDTO cambiarEstadoIncidencia(Long incidenciaId, EstadoIncidencia nuevoEstado) {
        if (nuevoEstado == null) {
            throw new RuntimeException("El nuevo estado es obligatorio");
        }

        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

        incidencia.setEstadoIncidencia(nuevoEstado);

        if (nuevoEstado == EstadoIncidencia.CERRADA) {
            incidencia.setFechaCierre(LocalDateTime.now());
        }

        Incidencia incidenciaActualizada = incidenciaRepository.save(incidencia);
        return convertirAResponseDTO(incidenciaActualizada);
    }

    @Override
    public List<EstadoIncidencia> obtenerEstadosIncidencia() {
        return List.of(EstadoIncidencia.values());
    }

    @Override
    @Transactional
    public MensajeIncidenciaResponseDTO responderIncidencia(Long incidenciaId, String mensaje) {
        if (mensaje == null || mensaje.trim().isBlank()) {
            throw new RuntimeException("El mensaje de respuesta es obligatorio");
        }

        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

        if (incidencia.getEstadoIncidencia() == EstadoIncidencia.CERRADA) {
            throw new RuntimeException("No se puede responder una incidencia cerrada");
        }

        MensajeIncidencia mensajeAdmin = new MensajeIncidencia();
        mensajeAdmin.setIncidencia(incidencia);
        mensajeAdmin.setRemitente(RemitenteMensajeIncidencia.ADMIN);
        mensajeAdmin.setOrigen(OrigenMensajeIncidencia.WEB);
        mensajeAdmin.setEmailRemitente("admin@moda.com");
        mensajeAdmin.setContenido(mensaje.trim());

        MensajeIncidencia mensajeGuardado = mensajeIncidenciaRepository.save(mensajeAdmin);

        incidencia.setEstadoIncidencia(EstadoIncidencia.ESPERANDO_RESPUESTA_USUARIO);
        incidenciaRepository.save(incidencia);

        enviarCorreoRespuestaAdmin(incidencia, mensajeGuardado.getContenido());

        return convertirMensajeAResponseDTO(mensajeGuardado);
    }

    @Override
    @Transactional
    public MensajeIncidenciaResponseDTO registrarRespuestaUsuarioDesdeEmail(String codigoSeguimiento,
                                                                            String emailRemitente,
                                                                            String contenido) {
        if (codigoSeguimiento == null || codigoSeguimiento.trim().isBlank()) {
            throw new RuntimeException("El codigo de seguimiento es obligatorio");
        }

        if (emailRemitente == null || emailRemitente.trim().isBlank()) {
            throw new RuntimeException("El email del remitente es obligatorio");
        }

        if (contenido == null || contenido.trim().isBlank()) {
            throw new RuntimeException("El contenido del mensaje es obligatorio");
        }

        Incidencia incidencia = incidenciaRepository.findByCodigoSeguimiento(codigoSeguimiento.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("No existe una incidencia con ese codigo de seguimiento"));

        if (incidencia.getEstadoIncidencia() == EstadoIncidencia.CERRADA) {
            throw new RuntimeException("No se puede responder una incidencia cerrada");
        }

        if (!incidencia.getEmailContacto().equalsIgnoreCase(emailRemitente.trim())) {
            throw new RuntimeException("El email del remitente no coincide con el email de la incidencia");
        }

        MensajeIncidencia mensajeUsuario = new MensajeIncidencia();
        mensajeUsuario.setIncidencia(incidencia);
        mensajeUsuario.setRemitente(RemitenteMensajeIncidencia.USUARIO);
        mensajeUsuario.setOrigen(OrigenMensajeIncidencia.EMAIL);
        mensajeUsuario.setEmailRemitente(emailRemitente.trim());
        mensajeUsuario.setContenido(contenido.trim());

        MensajeIncidencia mensajeGuardado = mensajeIncidenciaRepository.save(mensajeUsuario);

        incidencia.setEstadoIncidencia(EstadoIncidencia.RESPONDIDA_POR_USUARIO);
        incidenciaRepository.save(incidencia);

        return convertirMensajeAResponseDTO(mensajeGuardado);
    }

    private String construirRespuestaAdminHtml(Incidencia incidencia, String mensajeAdmin) {
        String plantilla = leerPlantillaHtml("templates/incidenciaRespuestaAdmin.html");

        return plantilla
                .replace("{{NOMBRE_CONTACTO}}", escaparHtml(formatearValorOpcional(incidencia.getNombreContacto(), "Cliente")))
                .replace("{{NUMERO_INCIDENCIA}}", escaparHtml(formatearNumeroIncidencia(incidencia)))
                .replace("{{CODIGO_SEGUIMIENTO}}", escaparHtml(formatearValorOpcional(incidencia.getCodigoSeguimiento(), "Sin codigo")))
                .replace("{{FECHA_INCIDENCIA}}", escaparHtml(formatearFecha(incidencia.getFechaCreacion())))
                .replace("{{ESTADO_INCIDENCIA}}", escaparHtml(formatearEstadoIncidencia(incidencia.getEstadoIncidencia())))
                .replace("{{TIPO_INCIDENCIA}}", escaparHtml(formatearTipoIncidencia(incidencia.getTipoIncidencia())))
                .replace("{{ASUNTO_INCIDENCIA}}", escaparHtml(formatearValorOpcional(incidencia.getAsunto(), "Sin asunto")))
                .replace("{{NUMERO_PEDIDO}}", escaparHtml(incidencia.getNumeroPedido() != null ? "#" + incidencia.getNumeroPedido() : "No asociado"))
                .replace("{{USUARIO_RELACIONADO}}", escaparHtml(formatearValorOpcional(incidencia.getUsuarioRelacionado(), "No indicado")))
                .replace("{{MENSAJE_INICIAL}}", formatearTextoLargo(incidencia.getMensajeInicial(), "No se incluyo un mensaje inicial."))
                .replace("{{MENSAJE_ADMIN}}", formatearTextoLargo(mensajeAdmin, "Nuestro equipo te respondera en cuanto haya novedades."));
    }

    private void enviarCorreoRespuestaAdmin(Incidencia incidencia, String mensajeAdmin) {
        try {
            String asunto = "[" + incidencia.getCodigoSeguimiento() + "] Respuesta a tu incidencia";
            String contenidoHtml = construirRespuestaAdminHtml(incidencia, mensajeAdmin);

            emailService.enviarCorreoHtmlConResultado(
                    incidencia.getEmailContacto(),
                    asunto,
                    contenidoHtml
            );
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO DE RESPUESTA DE INCIDENCIA: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void validarCrearIncidencia(CrearIncidenciaRequestDTO request) {
        if (request.getNombreContacto() == null || request.getNombreContacto().trim().isBlank()) {
            throw new RuntimeException("El nombre de contacto es obligatorio");
        }

        if (request.getEmailContacto() == null || request.getEmailContacto().trim().isBlank()) {
            throw new RuntimeException("El email de contacto es obligatorio");
        }

        if (!request.getEmailContacto().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("El formato del email no es valido");
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

    private EmailDispatchResult enviarCorreoConfirmacionIncidencia(Incidencia incidencia) {
        try {
            String asunto = "[" + incidencia.getCodigoSeguimiento() + "] Incidencia recibida";
            String contenidoHtml = construirIncidenciaCreadaHtml(incidencia);

            return emailService.enviarCorreoHtmlConResultado(
                    incidencia.getEmailContacto(),
                    asunto,
                    contenidoHtml
            );
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO DE CONFIRMACION DE INCIDENCIA: " + e.getMessage());
            e.printStackTrace();
            return EmailDispatchResult.pendiente(
                    "La incidencia ha quedado registrada. Enviaremos la confirmacion por correo en cuanto vuelva el servicio."
            );
        }
    }

    private String construirIncidenciaCreadaHtml(Incidencia incidencia) {
        String plantilla = leerPlantillaHtml("templates/incidenciaCreada.html");

        return plantilla
                .replace("{{NOMBRE_CONTACTO}}", escaparHtml(formatearValorOpcional(incidencia.getNombreContacto(), "Cliente")))
                .replace("{{NUMERO_INCIDENCIA}}", escaparHtml(formatearNumeroIncidencia(incidencia)))
                .replace("{{CODIGO_SEGUIMIENTO}}", escaparHtml(formatearValorOpcional(incidencia.getCodigoSeguimiento(), "Sin codigo")))
                .replace("{{FECHA_INCIDENCIA}}", escaparHtml(formatearFecha(incidencia.getFechaCreacion())))
                .replace("{{ESTADO_INCIDENCIA}}", escaparHtml(formatearEstadoIncidencia(incidencia.getEstadoIncidencia())))
                .replace("{{TIPO_INCIDENCIA}}", escaparHtml(formatearTipoIncidencia(incidencia.getTipoIncidencia())))
                .replace("{{ASUNTO_INCIDENCIA}}", escaparHtml(formatearValorOpcional(incidencia.getAsunto(), "Sin asunto")))
                .replace("{{NUMERO_PEDIDO}}", escaparHtml(incidencia.getNumeroPedido() != null ? "#" + incidencia.getNumeroPedido() : "No asociado"))
                .replace("{{USUARIO_RELACIONADO}}", escaparHtml(formatearValorOpcional(incidencia.getUsuarioRelacionado(), "No indicado")))
                .replace("{{MENSAJE_INICIAL}}", formatearTextoLargo(incidencia.getMensajeInicial(), "No se incluyo un mensaje inicial."));
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

    private IncidenciaResponseDTO convertirAResponseDTO(Incidencia incidencia, EmailDispatchResult resultadoCorreo) {
        IncidenciaResponseDTO dto = new IncidenciaResponseDTO(
                incidencia.getId(),
                incidencia.getCodigoSeguimiento(),
                incidencia.getNombreContacto(),
                incidencia.getEmailContacto(),
                incidencia.getTipoIncidencia(),
                incidencia.getEstadoIncidencia(),
                incidencia.getAsunto(),
                incidencia.getFechaCreacion()
        );

        if (resultadoCorreo != null) {
            dto.setCorreoPendiente(resultadoCorreo.isPendiente());
            dto.setMensajeCorreo(resultadoCorreo.getMensaje());
        }

        return dto;
    }

    private IncidenciaResponseDTO convertirAResponseDTO(Incidencia incidencia) {
        return convertirAResponseDTO(incidencia, null);
    }

    private MensajeIncidenciaResponseDTO convertirMensajeAResponseDTO(MensajeIncidencia mensaje) {
        return new MensajeIncidenciaResponseDTO(
                mensaje.getId(),
                mensaje.getIncidencia().getId(),
                mensaje.getRemitente(),
                mensaje.getOrigen(),
                mensaje.getEmailRemitente(),
                mensaje.getContenido(),
                mensaje.getFechaMensaje()
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
        if (tipoIncidencia == null) {
            return "No indicado";
        }

        return switch (tipoIncidencia) {
            case PROBLEMA_ACCESO -> "Problema de acceso";
            case NO_RECUERDO_DATOS -> "No recuerdo mis datos";
            case SIN_ACCESO_EMAIL -> "Sin acceso al email";
            case PROBLEMA_PEDIDO -> "Problema con un pedido";
            case PROBLEMA_PAGO -> "Problema con el pago";
            case PRODUCTO_DEFECTUOSO -> "Producto danado o incorrecto";
            case ERROR_WEB -> "Error en la web";
            case OTRO -> "Otro";
        };
    }

    private String formatearEstadoIncidencia(EstadoIncidencia estadoIncidencia) {
        if (estadoIncidencia == null) {
            return "Sin estado";
        }

        return switch (estadoIncidencia) {
            case PENDIENTE -> "Pendiente";
            case EN_REVISION -> "En revision";
            case ESPERANDO_RESPUESTA_USUARIO -> "Esperando respuesta del usuario";
            case RESPONDIDA_POR_USUARIO -> "Respondida por el usuario";
            case RESUELTA -> "Resuelta";
            case CERRADA -> "Cerrada";
        };
    }

    private String formatearNumeroIncidencia(Incidencia incidencia) {
        if (incidencia == null || incidencia.getId() == null) {
            return "Pendiente";
        }

        return "INC-" + String.format("%06d", incidencia.getId());
    }

    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "Sin fecha";
        }

        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatearValorOpcional(String valor, String fallback) {
        if (valor == null || valor.trim().isBlank()) {
            return fallback;
        }

        return valor.trim();
    }

    private String formatearTextoLargo(String valor, String fallback) {
        String texto = formatearValorOpcional(valor, fallback);
        return escaparHtml(texto).replace("\r\n", "\n").replace("\n", "<br>");
    }
}
