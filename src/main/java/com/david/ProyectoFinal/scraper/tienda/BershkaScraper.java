package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.scraper.config.ConfigScrapingTienda;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BershkaScraper implements ScraperTienda{

    @Override
    public String getNombreTienda() {
        return "Bershka";
    }

    private boolean esUrlProducto(String url) {
        return url != null && url.matches(".*/es/.*-c0p\\d+\\.html.*");
    }

    private void scrollHastaFin(Page page) throws InterruptedException {
        int anteriores = 0;
        int sinCambios = 0;

        while (sinCambios < 10) {
            List<String> urls = (List<String>) page.locator("a[href*='/es/'][href*='.html']")
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


    @Override
    public List<Producto> scrapearProductos() {
        List<Producto> productos = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            Tienda tienda = new Tienda();
            tienda.setNombre("Bershka");
            tienda.setUrl("https://www.bershka.com");

            List<ConfigScrapingTienda> configuraciones = List.of(
                    new ConfigScrapingTienda("https://www.bershka.com/es/hombre/ropa/camisetas-n3294.html?celement=1010193239", Seccion.HOMBRE, "Camisetas"),
                    new ConfigScrapingTienda("https://www.bershka.com/es/mujer/ropa/camisetas-n4365.html", Seccion.MUJER, "Camisetas")
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
                    Locator botonCookies = page.locator("button")
                            .filter(new Locator.FilterOptions().setHasText("Aceptar"))
                            .first();

                    if (botonCookies.count() > 0) {
                        botonCookies.click();
                    }

                    cookiesAceptadas = true;
                }

                scrollHastaFin(page);
                page.waitForTimeout(2000);

                List<String> urls = (List<String>) page.locator("a")
                        .evaluateAll("elements => elements.map(e => e.getAttribute('href'))");

                urls = urls.stream()
                        .filter(this::esUrlProducto)
                        .distinct()
                        .map(url -> "https://www.bershka.com" + url)
                        .toList();

                int limitePorListado = Math.min(300, urls.size());

                for (int i = 0; i < limitePorListado; i++) {
                    Producto producto = extraerProducto(page, urls.get(i), seccion, categoria, tienda);

                    if (producto != null &&
                            productos.stream().noneMatch(p -> p.getUrlProducto().equals(producto.getUrlProducto()))) {
                            productos.add(producto);
                    }
                }
            }

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
            page.waitForTimeout(4000);

            String descripcion = null;

            String nombre = page.locator("h1").nth(1).textContent().trim();

            String precioTexto = null;
            Locator spans = page.locator("span");

            for (int j = 0; j < spans.count(); j++) {
                String texto = spans.nth(j).textContent();

                if (texto != null && texto.contains("€") && texto.length() < 20) {
                    precioTexto = texto.trim();
                    break;
                }
            }

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
                .replace("\u00A0", "")
                .trim();

        return new BigDecimal(limpio);
    }
}
