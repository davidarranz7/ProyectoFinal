package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class PullAndBearScraper implements ScraperTienda{

    @Override
    public String getNombreTienda() {
        return "PullAndBear";
    }

    @Override
    public List<Producto> scrapearProductos() {
        return new ArrayList<>();
    }
}
