package com.david.ProyectoFinal.scraper.gestor;

import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.scraper.tienda.BershkaScraper;
import com.david.ProyectoFinal.scraper.tienda.PullAndBearScraper;
import com.david.ProyectoFinal.scraper.tienda.ScraperTienda;
import com.david.ProyectoFinal.scraper.tienda.ZaraScraper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GestorScraping {

    private List<ScraperTienda> scrapers;

    public GestorScraping() {
        this.scrapers = new ArrayList<>();

        scrapers.add(new ZaraScraper());
        scrapers.add(new BershkaScraper());
        scrapers.add(new PullAndBearScraper());
    }

    public void agregarScraper(ScraperTienda scraper) {
        scrapers.add(scraper);
    }

    public List<Producto> scrapearTodo() {
        List<Producto> productos = new ArrayList<>();

        for (ScraperTienda scraper : scrapers) {
            productos.addAll(scraper.scrapearProductos());
        }

        return productos;
    }

    public List<Producto> scrapearZara() {
        List<Producto> productos = new ArrayList<>();

        for (ScraperTienda scraper : scrapers) {
            if (scraper instanceof ZaraScraper) {
                productos.addAll(scraper.scrapearProductos());
            }
        }

        return productos;
    }

    public List<Producto> scrapearBershka() {
        List<Producto> productos = new ArrayList<>();

        for (ScraperTienda scraper : scrapers) {
            if (scraper instanceof BershkaScraper) {
                productos.addAll(scraper.scrapearProductos());
            }
        }

        return productos;
    }

    public List<Producto> scrapearPullAndBear() {
        List<Producto> productos = new ArrayList<>();

        for (ScraperTienda scraper : scrapers) {
            if (scraper instanceof PullAndBearScraper) {
                productos.addAll(scraper.scrapearProductos());
            }
        }

        return productos;
    }
}