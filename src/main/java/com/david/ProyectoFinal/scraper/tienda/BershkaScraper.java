package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class BershkaScraper implements ScraperTienda{

    @Override
    public String getNombreTienda() {
        return "Bershka";
    }

    @Override
    public List<Producto> scrapearProductos() {
        return new ArrayList<>();
    }
}
