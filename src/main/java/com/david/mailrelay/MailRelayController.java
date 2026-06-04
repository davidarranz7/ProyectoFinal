package com.david.mailrelay;

import com.david.ProyectoFinal.dto.MailRelayRequestDTO;
import com.david.ProyectoFinal.dto.ScrapingRelayRequestDTO;
import com.david.ProyectoFinal.dto.ScrapingRelayResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/mail-relay")
public class MailRelayController {

    private final MailRelaySmtpService mailRelaySmtpService;
    private final MailRelayScrapingService mailRelayScrapingService;

    @Value("${app.mail.relay.allowed-server-ip:}")
    private String allowedServerIp;

    @Value("${app.mail.relay.token:}")
    private String relayToken;

    public MailRelayController(MailRelaySmtpService mailRelaySmtpService,
                               MailRelayScrapingService mailRelayScrapingService) {
        this.mailRelaySmtpService = mailRelaySmtpService;
        this.mailRelayScrapingService = mailRelayScrapingService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> recibirCorreo(@RequestBody MailRelayRequestDTO request,
                                                @RequestHeader(name = "X-Relay-Token", required = false) String token,
                                                HttpServletRequest httpServletRequest) {
        validarPeticionRelay(token, httpServletRequest);
        mailRelaySmtpService.enviar(request);
        return ResponseEntity.ok("Correo aceptado por el relay local");
    }

    @PostMapping("/scraping")
    public ResponseEntity<ScrapingRelayResponseDTO> ejecutarScraping(
            @RequestBody ScrapingRelayRequestDTO request,
            @RequestHeader(name = "X-Relay-Token", required = false) String token,
            HttpServletRequest httpServletRequest
    ) {
        validarPeticionRelay(token, httpServletRequest);
        return ResponseEntity.ok(mailRelayScrapingService.ejecutar(request));
    }

    private void validarPeticionRelay(String token, HttpServletRequest httpServletRequest) {
        if (relayToken == null || relayToken.isBlank() || !relayToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token de relay no valido");
        }

        String ipRemota = obtenerIpRemota(httpServletRequest);

        if (allowedServerIp != null && !allowedServerIp.isBlank() && !allowedServerIp.equals(ipRemota)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IP no autorizada");
        }
    }

    private String obtenerIpRemota(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
