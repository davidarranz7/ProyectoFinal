package com.david.mailrelay;

import com.david.ProyectoFinal.dto.ScrapingRelayRequestDTO;
import com.david.ProyectoFinal.dto.ScrapingRelayResponseDTO;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.TipoScrapingPendiente;
import com.david.ProyectoFinal.scraper.gestor.GestorScraping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailRelayScrapingService {

    public ScrapingRelayResponseDTO ejecutar(ScrapingRelayRequestDTO request) {
        TipoScrapingPendiente tipoScraping = TipoScrapingPendiente.valueOf(request.getTipo());
        GestorScraping gestorScraping = new GestorScraping();

        List<Producto> productos = switch (tipoScraping) {
            case TOTAL -> gestorScraping.scrapearTodo();
            case ZARA -> gestorScraping.scrapearZara();
            case BERSHKA -> gestorScraping.scrapearBershka();
            case PULL_AND_BEAR -> gestorScraping.scrapearPullAndBear();
        };

        ScrapingRelayResponseDTO respuesta = new ScrapingRelayResponseDTO();
        respuesta.setTipo(tipoScraping.name());
        respuesta.setNombreProceso(tipoScraping.getNombreProceso());
        respuesta.setProductos(productos);
        return respuesta;
    }
}
