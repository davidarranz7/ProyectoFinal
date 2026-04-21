package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.scraper.config.ConfigScrapingTienda;
import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.microsoft.playwright.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

        while (sinCambios < 10) {
            List<String> urls = (List<String>) page.locator("a[href*='/es/es/'][href*='.html']")
                    .evaluateAll("elements => elements.map(e => e.getAttribute('href'))");

            urls = urls.stream()
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
            page.waitForTimeout(2000);
        }
    }

    /// Recogida de Urls
    @Override
    public List<Producto> scrapearProductos() {
        List<Producto> productos = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();

            Tienda tienda = new Tienda();
            tienda.setNombre("Zara");
            tienda.setUrl("https://www.zara.com");

            List<ConfigScrapingTienda> configuraciones = List.of(
                    new ConfigScrapingTienda("https://www.zara.com/es/es/hombre-camisetas-l855.html", Seccion.HOMBRE, "Camisetas")
                    //new ConfigScrapingTienda("https://www.zara.com/es/es/hombre-pantalones-l838.html", Seccion.HOMBRE, "Pantalones"),
                    //new ConfigScrapingTienda("https://www.zara.com/es/es/mujer-pantalones-l1335.html", Seccion.MUJER, "Pantalones")
            );

            boolean cookiesAceptadas = false;

            for (ConfigScrapingTienda config : configuraciones) {
                String urlListado = config.getUrl();
                Seccion seccion = config.getSeccion();
                String nombreCategoria = config.getNombreCategoria();

                Categoria categoria = new Categoria();
                categoria.setNombre(nombreCategoria);

                page.navigate(urlListado);

                if (!cookiesAceptadas) {
                    page.locator("button")
                            .filter(new Locator.FilterOptions().setHasText("Aceptar"))
                            .first()
                            .click();
                    cookiesAceptadas = true;
                }

                scrollHastaFin(page);

                List<String> urls = (List<String>) page.locator("a[href*='/es/es/'][href*='.html']")
                        .evaluateAll("elements => elements.map(e => e.getAttribute('href'))");

                urls = urls.stream()
                        .filter(this::esUrlProducto)
                        .distinct()
                        .toList();

                int limitePorListado = Math.min(300, urls.size());

                for (int i = 0; i < limitePorListado; i++) {
                    Producto producto = extraerProducto(page, urls.get(i),seccion, categoria,tienda);

                    if (producto != null &&
                            productos.stream().noneMatch(p -> p.getUrlProducto().equals(producto.getUrlProducto()))) {
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


    private Producto extraerProducto(Page page, String urlProducto, Seccion seccion, Categoria categoria, Tienda tienda) {
        try {
            page.navigate(urlProducto);

            ///Thread.sleep(2000);
            page.waitForSelector("h1");

            String descripcion = null;
            Locator descripcionLocator = page.locator("div.expandable-text__inner-content p").first();

            if (descripcionLocator.count() > 0) {
                descripcion = descripcionLocator.textContent().trim();
            }

            String nombre = page.locator("h1").first().textContent().trim();
            String precioTexto = page.locator("span.money-amount__main").first().textContent();
            BigDecimal precio = convertirPrecio(precioTexto);

            if (nombre == null || nombre.isBlank() || precio == null) {
                return null;
            }

            String imagen = page.locator("meta[property='og:image']").first().getAttribute("content");

            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setUrlProducto(urlProducto);
            producto.setPrecio(precio);
            producto.setSeccion(seccion);
            producto.setUrlImagen(imagen);
            producto.setDescripcion(descripcion);

            producto.setTienda(tienda);
            producto.setCategoria(categoria);

            return producto;

        } catch (Exception e) {
            System.out.println("ERROR en producto: " + urlProducto);
            e.printStackTrace();
            return null;
        }

    }

    private BigDecimal convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.isBlank()) {
            return null;
        }

        String limpio = precioTexto
                .replace("EUR", "")
                .replace("€", "")
                .replace(",", ".")
                .trim();

        return new BigDecimal(limpio);
    }


}
