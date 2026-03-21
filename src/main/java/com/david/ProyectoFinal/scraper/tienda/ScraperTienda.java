package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Producto;

import java.util.List;

public interface ScraperTienda {

    String getNombreTienda();

    List<Producto> scrapearProductos();

}
