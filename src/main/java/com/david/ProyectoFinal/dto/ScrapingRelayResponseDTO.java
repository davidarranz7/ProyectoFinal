package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Producto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScrapingRelayResponseDTO {

    private String tipo;
    private String nombreProceso;
    private List<Producto> productos = new ArrayList<>();
    private Boolean scrapingDisponible = true;
    private String mensajeRelay;

    public ScrapingRelayResponseDTO() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setNombreProceso(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos == null ? new ArrayList<>() : productos;
    }

    public Boolean getScrapingDisponible() {
        return scrapingDisponible;
    }

    public void setScrapingDisponible(Boolean scrapingDisponible) {
        this.scrapingDisponible = scrapingDisponible;
    }

    public String getMensajeRelay() {
        return mensajeRelay;
    }

    public void setMensajeRelay(String mensajeRelay) {
        this.mensajeRelay = mensajeRelay;
    }
}
