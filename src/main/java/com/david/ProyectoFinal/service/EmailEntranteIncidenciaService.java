package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.EstadoIncidencia;
import com.david.ProyectoFinal.model.Incidencia;
import com.david.ProyectoFinal.model.MensajeIncidencia;
import com.david.ProyectoFinal.model.OrigenMensajeIncidencia;
import com.david.ProyectoFinal.model.RemitenteMensajeIncidencia;
import com.david.ProyectoFinal.repository.IncidenciaRepository;
import com.david.ProyectoFinal.repository.MensajeIncidenciaRepository;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailEntranteIncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final MensajeIncidenciaRepository mensajeIncidenciaRepository;

    @Value("${app.mail.imap.host}")
    private String imapHost;

    @Value("${app.mail.imap.port}")
    private String imapPort;

    @Value("${app.mail.imap.username}")
    private String imapUsername;

    @Value("${app.mail.imap.password}")
    private String imapPassword;

    private static final Pattern PATRON_CODIGO_INCIDENCIA = Pattern.compile("INC-[A-Z0-9]{8}");

    public EmailEntranteIncidenciaService(IncidenciaRepository incidenciaRepository,
                                          MensajeIncidenciaRepository mensajeIncidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.mensajeIncidenciaRepository = mensajeIncidenciaRepository;
    }

    @Transactional
    public synchronized int leerCorreosEntrantesDeIncidencias() {
        int correosProcesados = 0;

        Store store = null;
        Folder inbox = null;

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", imapHost);
            properties.put("mail.imaps.port", imapPort);
            properties.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(properties);

            store = session.getStore("imaps");
            store.connect(imapHost, imapUsername, imapPassword);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] mensajesNoLeidos = inbox.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false)
            );

            for (Message mensajeEmail : mensajesNoLeidos) {
                boolean procesado = procesarMensajeEmail(mensajeEmail);

                if (procesado) {
                    mensajeEmail.setFlag(Flags.Flag.SEEN, true);
                    correosProcesados++;
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR AL LEER CORREOS ENTRANTES DE INCIDENCIAS: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(inbox, store);
        }

        return correosProcesados;
    }

    private boolean procesarMensajeEmail(Message mensajeEmail) {
        try {
            String asunto = mensajeEmail.getSubject();

            String codigoIncidencia = extraerCodigoIncidencia(asunto);

            if (codigoIncidencia == null) {
                return false;
            }

            Incidencia incidencia = incidenciaRepository.findByCodigoSeguimiento(codigoIncidencia)
                    .orElse(null);

            if (incidencia == null) {
                return false;
            }

            if (incidencia.getEstadoIncidencia() == EstadoIncidencia.CERRADA) {
                return false;
            }

            String emailRemitente = obtenerEmailRemitente(mensajeEmail);
            String contenido = extraerContenidoTexto(mensajeEmail);

            if (contenido == null || contenido.trim().isBlank()) {
                return false;
            }

            MensajeIncidencia mensajeUsuario = new MensajeIncidencia();
            mensajeUsuario.setIncidencia(incidencia);
            mensajeUsuario.setRemitente(RemitenteMensajeIncidencia.USUARIO);
            mensajeUsuario.setOrigen(OrigenMensajeIncidencia.EMAIL);
            mensajeUsuario.setEmailRemitente(emailRemitente);
            mensajeUsuario.setContenido(limpiarRespuestaEmail(contenido));

            mensajeIncidenciaRepository.save(mensajeUsuario);

            incidencia.setEstadoIncidencia(EstadoIncidencia.RESPONDIDA_POR_USUARIO);
            incidenciaRepository.save(incidencia);

            return true;

        } catch (Exception e) {
            System.out.println("ERROR AL PROCESAR EMAIL DE INCIDENCIA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String extraerCodigoIncidencia(String asunto) {
        if (asunto == null || asunto.isBlank()) {
            return null;
        }

        Matcher matcher = PATRON_CODIGO_INCIDENCIA.matcher(asunto);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    private String obtenerEmailRemitente(Message mensajeEmail) throws Exception {
        if (mensajeEmail.getFrom() == null || mensajeEmail.getFrom().length == 0) {
            return "";
        }

        if (mensajeEmail.getFrom()[0] instanceof InternetAddress internetAddress) {
            return internetAddress.getAddress();
        }

        return mensajeEmail.getFrom()[0].toString();
    }

    private String extraerContenidoTexto(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        }

        if (part.isMimeType("text/html")) {
            String html = part.getContent().toString();
            return limpiarHtml(html);
        }

        if (part.isMimeType("multipart/alternative")) {
            Multipart multipart = (Multipart) part.getContent();

            String textoHtml = "";

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (bodyPart.isMimeType("text/plain")) {
                    String textoPlano = extraerContenidoTexto(bodyPart);
                    if (textoPlano != null && !textoPlano.isBlank()) {
                        return textoPlano;
                    }
                }

                if (bodyPart.isMimeType("text/html")) {
                    textoHtml = extraerContenidoTexto(bodyPart);
                }
            }

            return textoHtml;
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    continue;
                }

                String texto = extraerContenidoTexto(bodyPart);

                if (texto != null && !texto.isBlank()) {
                    return texto;
                }
            }
        }

        return "";
    }

    private String limpiarHtml(String html) {
        if (html == null) {
            return "";
        }

        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private String limpiarRespuestaEmail(String contenido) {
        if (contenido == null) {
            return "";
        }

        String limpio = contenido
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        limpio = cortarDesdeMarcadorRespuestaAnterior(limpio);
        limpio = eliminarLineasCitadas(limpio);
        limpio = eliminarLineasVaciasDuplicadas(limpio);

        return limpio.trim();
    }

    private String cortarDesdeMarcadorRespuestaAnterior(String texto) {
        String[] marcadores = {
                "\nEl ",
                "\nOn ",
                "\nDe:",
                "\nFrom:",
                "\nEnviado:",
                "\nSent:",
                "\nPara:",
                "\nTo:",
                "\nAsunto:",
                "\nSubject:",
                "\n---------- Forwarded message"
        };

        int corte = -1;

        for (String marcador : marcadores) {
            int indice = texto.indexOf(marcador);

            if (indice > 0 && (corte == -1 || indice < corte)) {
                corte = indice;
            }
        }

        if (corte > 0) {
            return texto.substring(0, corte).trim();
        }

        return texto;
    }

    private String eliminarLineasCitadas(String texto) {
        StringBuilder resultado = new StringBuilder();
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            String lineaLimpia = linea.trim();

            if (lineaLimpia.startsWith(">")) {
                continue;
            }

            if (lineaLimpia.equalsIgnoreCase("escribió:")) {
                continue;
            }

            resultado.append(linea).append("\n");
        }

        return resultado.toString().trim();
    }

    private String eliminarLineasVaciasDuplicadas(String texto) {
        return texto
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private void cerrarRecursos(Folder inbox, Store store) {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
            }

            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (Exception e) {
            System.out.println("ERROR AL CERRAR CONEXIÓN IMAP: " + e.getMessage());
        }
    }
}