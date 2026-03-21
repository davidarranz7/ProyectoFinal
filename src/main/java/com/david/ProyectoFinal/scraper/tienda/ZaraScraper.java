package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.scraper.config.ConfigScrapingZara;
import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZaraScraper implements ScraperTienda {

    @Override
    public String getNombreTienda() {
        return "Zara";
    }

    /// Aqui validamos la url que sean validas y que no sean banners
    private boolean esUrlProducto(String url) {
        return url != null && url.matches(".*-p\\d+\\.html$");
    }

    /// Aqui validamos que el scroll sea perfecto llegando hasta el final
    private void scrollHastaFin(Page page) throws InterruptedException {
        int anteriores = 0;
        int sinCambios = 0;

        while (sinCambios < 3) {
            List<String> urls = page.locator("a[href*='/es/es/'][href*='.html']")
                    .all()
                    .stream()
                    .map(e -> e.getAttribute("href"))
                    .filter(this::esUrlProducto)
                    .distinct()
                    .toList();

            int actuales = urls.size();

            if (actuales == anteriores) {
                sinCambios++;
            } else {
                sinCambios = 0;
                anteriores = actuales;
            }

            page.mouse().wheel(0, 2500);
            Thread.sleep(2000);
        }

    }

    /// Recogida de Urls
    @Override


    /// Convertir a objeto las URL
    public List<Producto> scrapearProductos() {
        List<Producto> productos = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            List<ConfigScrapingZara> configuraciones = List.of(
                    new ConfigScrapingZara("https://www.zara.com/es/es/hombre-camisetas-l855.html", Seccion.HOMBRE, "Camisetas"),
                    new ConfigScrapingZara("https://www.zara.com/es/es/hombre-pantalones-l838.html", Seccion.HOMBRE, "Pantalones"),
                    new ConfigScrapingZara("https://www.zara.com/es/es/mujer-pantalones-l1335.html", Seccion.MUJER, "Pantalones")
            );

            boolean cookiesAceptadas = false;

            for (ConfigScrapingZara config : configuraciones) {
                String urlListado = config.getUrl();
                Seccion seccion = config.getSeccion();
                String nombreCategoria = config.getNombreCategoria();

                page.navigate(urlListado);

                if (!cookiesAceptadas) {
                    page.locator("button")
                            .filter(new Locator.FilterOptions().setHasText("Aceptar"))
                            .first()
                            .click();
                    cookiesAceptadas = true;
                }

                scrollHastaFin(page);

                List<String> urls = page.locator("a[href*='/es/es/'][href*='.html']")
                        .all()
                        .stream()
                        .map(e -> e.getAttribute("href"))
                        .filter(this::esUrlProducto)
                        .distinct()
                        .toList();

                int limitePorListado = Math.min(5, urls.size());

                for (int i = 0; i < limitePorListado; i++) {
                    Producto producto = extraerProducto(page, urls.get(i),seccion, nombreCategoria);

                    if (producto != null) {
                        productos.add(producto);
                    }
                }
            }

            System.out.println("Productos reales detectados: " + productos.size());

            Thread.sleep(5000);
            browser.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return productos;
    }
    private Producto extraerProducto(Page page, String urlProducto, Seccion seccion, String nombreCategoria) {
        try {
            page.navigate(urlProducto);

            Thread.sleep(2000);

            String nombre = page.locator("h1").first().textContent();
            String precioTexto = page.locator("span.money-amount__main").first().textContent();
            java.math.BigDecimal precio = convertirPrecio(precioTexto);

            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setUrlProducto(urlProducto);
            producto.setPrecio(precio);
            producto.setSeccion(seccion);

            Tienda tienda = new Tienda();
            tienda.setNombre("Zara");
            tienda.setUrl("https://www.zara.com");
            producto.setTienda(tienda);

            Categoria categoria = new Categoria();
            categoria.setNombre(nombreCategoria);
            producto.setCategoria(categoria);

            return producto;

        } catch (Exception e) {
            return null;
        }

    }

    private java.math.BigDecimal convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.isBlank()) {
            return null;
        }

        String limpio = precioTexto
                .replace("EUR", "")
                .replace("€", "")
                .replace(",", ".")
                .trim();

        return new java.math.BigDecimal(limpio);
    }
}
