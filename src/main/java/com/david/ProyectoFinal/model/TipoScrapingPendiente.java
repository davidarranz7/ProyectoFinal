package com.david.ProyectoFinal.model;

public enum TipoScrapingPendiente {
    TOTAL("Scraping completo"),
    ZARA("Zara"),
    BERSHKA("Bershka"),
    PULL_AND_BEAR("Pull&Bear");

    private final String nombreProceso;

    TipoScrapingPendiente(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }
}
