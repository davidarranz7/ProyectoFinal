package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.scraper.config.ConfigScrapingTienda;
import com.microsoft.playwright.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PullAndBearScraper implements ScraperTienda{

    @Override
    public String getNombreTienda() {
        return "PullAndBear";
    }

    private boolean esUrlProducto(String url) {
        return url != null
                && url.matches(".*-l\\d+.*")
                && url.contains("pelement=")
                && url.contains("pullandbear.com");
    }

    private void scrollHastaFin(Page page) throws InterruptedException {
        int anteriores = 0;
        int sinCambios = 0;

        while (sinCambios < 10) {
            List<String> urls = (List<String>) page.locator("a")
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
            tienda.setNombre("PullAndBear");
            tienda.setUrl("https://www.pullandbear.com");

            List<ConfigScrapingTienda> configuraciones = List.of(
                    new ConfigScrapingTienda("https://www.pullandbear.com/es/hombre/ropa/camisetas-n6323", Seccion.HOMBRE, "Camisetas"),
                    new ConfigScrapingTienda("https://www.pullandbear.com/es/mujer/ropa/camisetas-n6541", Seccion.MUJER, "Camisetas")
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

                System.out.println("TOTAL LINKS: " + urls.size());

                urls = urls.stream()
                        .filter(this::esUrlProducto)
                        .distinct()
                        .toList();

                System.out.println("PRODUCTOS DETECTADOS: " + urls.size());

                int limitePorListado = Math.min(300, urls.size());

                for (int i = 0; i < limitePorListado; i++) {
                    Producto producto = extraerProducto(page, urls.get(i), seccion, categoria, tienda);

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
            page.waitForTimeout(4000);

            String descripcion = null;
            Locator descripcionLocator = page.locator("p.long-description").first();

            if (descripcionLocator.count() > 0) {
                descripcion = descripcionLocator.textContent().trim();
            }

            String nombre = page.locator("h1").first().textContent().trim();

            String precioTexto = page.locator("p")
                    .filter(new Locator.FilterOptions().setHasText("€"))
                    .last()
                    .textContent()
                    .trim();

            BigDecimal precio = convertirPrecio(precioTexto);

            String imagen = page.locator("meta[property='og:image']").first().getAttribute("content");

            if (nombre == null || nombre.isBlank() || precio == null || imagen == null || imagen.isBlank()) {
                return null;
            }

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
