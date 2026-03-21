package com.david.ProyectoFinal.scraper.config;


import com.david.ProyectoFinal.model.Seccion;

public class ConfigScrapingZara {

    private String url;
    private Seccion seccion;
    private String nombreCategoria;

    public ConfigScrapingZara(String url, Seccion seccion, String nombreCategoria) {
        this.url = url;
        this.seccion = seccion;
        this.nombreCategoria = nombreCategoria;
    }

    public String getUrl() {
        return url;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }
}