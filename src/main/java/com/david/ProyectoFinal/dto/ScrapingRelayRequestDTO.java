package com.david.ProyectoFinal.dto;

public class ScrapingRelayRequestDTO {

    private String tipo;

    public ScrapingRelayRequestDTO() {
    }

    public ScrapingRelayRequestDTO(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
