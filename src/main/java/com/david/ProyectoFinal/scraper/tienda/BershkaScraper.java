package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.ProductoImagen;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BershkaScraper implements ScraperTienda {

    private static final String BASE_URL = "https://www.bershka.com";
    private static final String STORE_ID = "44009500";
    private static final String CATALOG_ID = "40259530";
    private static final String LANGUAGE_ID = "-5";
    private static final String LOCALE = "es_ES";

    private static final int TAMANO_BLOQUE_PRODUCTOS = 20;
    private static final int PAUSA_ENTRE_BLOQUES_MS = 0;
    private static final int PAUSA_ENTRE_CATEGORIAS_MS = 0;
    private static final int PAUSA_REINTENTO_403_MS = 800;

    private static final boolean HEADLESS = true;
    private static final boolean MODO_UNA_CATEGORIA_DEBUG = false;
    private static final boolean USAR_CATEGORIAS_WEB = true;

    private static final int MAX_IMAGENES_POR_PRODUCTO = 8;

    private final ObjectMapper objectMapper = crearObjectMapper();

    private ObjectMapper crearObjectMapper() {
        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxStringLength(100_000_000)
                        .maxNumberLength(100_000)
                        .maxNestingDepth(2_000)
                        .build())
                .build();

        return new ObjectMapper(jsonFactory);
    }

    @Override
    public String getNombreTienda() {
        return "Bershka";
    }

    @Override
    public List<Producto> scrapearProductos() {
        Map<String, Producto> productosGlobales = new LinkedHashMap<>();

        Tienda tienda = new Tienda();
        tienda.setNombre("Bershka");
        tienda.setUrl(BASE_URL);

        int categoriasOk = 0;
        int categoriasFallidas = 0;

        System.out.println("====================================");
        System.out.println("SCRAPING BERSHKA - PLAYWRIGHT FETCH API");
        System.out.println("====================================");
        System.out.println("Modo una categoría debug: " + MODO_UNA_CATEGORIA_DEBUG);
        System.out.println("Headless: " + HEADLESS);
        System.out.println("Store ID: " + STORE_ID);
        System.out.println("Catalog ID: " + CATALOG_ID);
        System.out.println("Language ID: " + LANGUAGE_ID);
        System.out.println("Locale: " + LOCALE);
        System.out.println();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = lanzarNavegador(playwright);

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setLocale("es-ES")
                    .setTimezoneId("Europe/Madrid")
                    .setViewportSize(1366, 768)
                    .setUserAgent(userAgent())
                    .setExtraHTTPHeaders(Map.of(
                            "Accept-Language", "es-ES,es;q=0.9"
                    ))
            );

            Page page = context.newPage();
            page.setDefaultTimeout(45000);

            prepararPaginaInicial(page);

            List<CategoriaBershka> categoriasBershka;

            if (MODO_UNA_CATEGORIA_DEBUG) {
                categoriasBershka = obtenerCategoriaDebug();
            } else if (USAR_CATEGORIAS_WEB) {
                categoriasBershka = obtenerCategoriasBershkaDesdeWeb(page);

                if (categoriasBershka.isEmpty()) {
                    System.out.println("No se detectaron categorías desde web. Usando fallback básico.");
                    categoriasBershka = obtenerCategoriasBershkaFallback();
                }
            } else {
                categoriasBershka = obtenerCategoriasBershkaFallback();
            }

            System.out.println("Categorías a procesar: " + categoriasBershka.size());
            System.out.println();

            for (CategoriaBershka categoriaBershka : categoriasBershka) {
                boolean categoriaProcesada = procesarCategoria(page, categoriaBershka, productosGlobales, tienda);

                if (categoriaProcesada) {
                    categoriasOk++;
                } else {
                    categoriasFallidas++;
                }

                esperar(PAUSA_ENTRE_CATEGORIAS_MS);
            }

            context.close();
            browser.close();

        } catch (Exception e) {
            System.out.println("ERROR GENERAL EN SCRAPER BERSHKA");
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }

        List<Producto> productosFinales = new ArrayList<>(productosGlobales.values());

        imprimirResumenFinal(productosFinales, categoriasOk, categoriasFallidas);

        return productosFinales;
    }

    private Browser lanzarNavegador(Playwright playwright) {
        try {
            System.out.println("Intentando abrir Google Chrome real...");
            return playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("chrome")
                    .setHeadless(HEADLESS)
            );
        } catch (PlaywrightException e) {
            System.out.println("No se pudo abrir Google Chrome real. Usando Chromium de Playwright...");
            return playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(HEADLESS)
            );
        }
    }

    private void prepararPaginaInicial(Page page) {
        System.out.println("====================================");
        System.out.println("PREPARANDO NAVEGADOR BERSHKA");
        System.out.println("====================================");

        try {
            page.navigate(BASE_URL + "/es/", new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(60000)
            );

            esperar(3000);
            aceptarCookiesSiAparece(page);

            System.out.println("Página inicial cargada.");
            System.out.println("URL actual: " + page.url());
            System.out.println();

        } catch (Exception e) {
            System.out.println("No se pudo preparar la página inicial.");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println();
        }
    }

    private void aceptarCookiesSiAparece(Page page) {
        try {
            page.locator("button:has-text('Aceptar')").first().click(new Locator.ClickOptions().setTimeout(2500));
            esperar(1000);
            System.out.println("Cookies aceptadas.");
        } catch (Exception ignored) {
        }
    }

    private List<CategoriaBershka> obtenerCategoriaDebug() {
        return List.of(
                new CategoriaBershka(
                        1010193217L,
                        Seccion.MUJER,
                        "Camisetas",
                        "Camisetas",
                        "mujer/ropa/camisetas-c1010193217.html?celement=1010193217",
                        "TEST > Mujer > Ropa > Camisetas"
                )
        );
    }

    private List<CategoriaBershka> obtenerCategoriasBershkaFallback() {
        return List.of(
                new CategoriaBershka(1010193217L, Seccion.MUJER, "Camisetas", "Camisetas", "mujer/ropa/camisetas-c1010193217.html?celement=1010193217", "Mujer > Ropa > Camisetas"),
                new CategoriaBershka(1010193239L, Seccion.HOMBRE, "Camisetas", "Camisetas", "hombre/ropa/camisetas-c1010193239.html?celement=1010193239", "Hombre > Ropa > Camisetas"),
                new CategoriaBershka(1010193241L, Seccion.HOMBRE, "Pantalones", "Pantalones", "hombre/ropa/pantalones-c1010193241.html?celement=1010193241", "Hombre > Ropa > Pantalones"),
                new CategoriaBershka(1010193244L, Seccion.HOMBRE, "Sudaderas", "Sudaderas", "hombre/ropa/sudaderas-c1010193244.html?celement=1010193244", "Hombre > Ropa > Sudaderas")
        );
    }

    private List<CategoriaBershka> obtenerCategoriasBershkaDesdeWeb(Page page) {
        List<CategoriaBershka> categorias = new ArrayList<>();
        Set<Long> idsVistos = new LinkedHashSet<>();

        System.out.println("====================================");
        System.out.println("BUSCANDO CATEGORÍAS BERSHKA DESDE WEB");
        System.out.println("====================================");

        try {
            abrirMenusPrincipales(page);

            String script = """
                    () => {
                        const enlaces = Array.from(document.querySelectorAll("a[href]"));

                        return JSON.stringify(enlaces.map(a => ({
                            href: a.href || "",
                            texto: (a.innerText || a.textContent || "").trim(),
                            aria: a.getAttribute("aria-label") || "",
                            title: a.getAttribute("title") || ""
                        })));
                    }
                    """;

            Object resultado = page.evaluate(script);
            JsonNode enlaces = objectMapper.readTree(String.valueOf(resultado));

            if (!enlaces.isArray()) {
                return categorias;
            }

            for (JsonNode enlace : enlaces) {
                String href = texto(enlace, "href");
                String texto = texto(enlace, "texto");
                String aria = texto(enlace, "aria");
                String title = texto(enlace, "title");

                String textoEnlace = !estaVacio(texto) ? texto : !estaVacio(aria) ? aria : title;

                if (estaVacio(href)) {
                    continue;
                }

                if (!esUrlCategoriaValidaBershka(href)) {
                    continue;
                }

                Long idCategoria = extraerIdCategoriaBershka(href);

                if (idCategoria == null) {
                    continue;
                }

                if (idsVistos.contains(idCategoria)) {
                    continue;
                }

                Seccion seccion = detectarSeccionDesdeUrl(href);
                String categoria = detectarCategoriaDesdeUrlYTexto(href, textoEnlace);
                String nombre = !estaVacio(textoEnlace) ? textoEnlace : categoria;
                String urlRelativa = convertirAUrlRelativaBershka(href);
                String ruta = seccion + " > " + categoria + " > " + nombre;

                if (categoria.equals("Otros")) {
                    continue;
                }

                categorias.add(new CategoriaBershka(
                        idCategoria,
                        seccion,
                        categoria,
                        nombre,
                        urlRelativa,
                        ruta
                ));

                idsVistos.add(idCategoria);
            }

            System.out.println("Categorías detectadas desde enlaces: " + categorias.size());

            categorias.forEach(categoria ->
                    System.out.println(categoria.id() + " | " + categoria.ruta() + " | " + categoria.url())
            );

            System.out.println();

        } catch (Exception e) {
            System.out.println("No se pudieron detectar categorías automáticamente.");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println();
        }

        return categorias;
    }

    private void abrirMenusPrincipales(Page page) {
        try {
            page.mouse().wheel(0, 500);
            esperar(500);
            page.mouse().wheel(0, -500);
            esperar(500);
        } catch (Exception ignored) {
        }

        List<String> selectoresMenu = List.of(
                "button:has-text('Menú')",
                "button:has-text('Menu')",
                "button[aria-label*='menu' i]",
                "button[aria-label*='menú' i]",
                "[data-testid*='menu' i]",
                "button:has-text('Mujer')",
                "a:has-text('Mujer')",
                "button:has-text('Hombre')",
                "a:has-text('Hombre')"
        );

        for (String selector : selectoresMenu) {
            try {
                page.locator(selector).first().click(new Locator.ClickOptions().setTimeout(1500));
                esperar(700);
            } catch (Exception ignored) {
            }
        }

        try {
            page.locator("a:has-text('Mujer'), button:has-text('Mujer')").first()
                    .click(new Locator.ClickOptions().setTimeout(2500));
            esperar(1000);
        } catch (Exception ignored) {
        }

        try {
            page.locator("a:has-text('Hombre'), button:has-text('Hombre')").first()
                    .click(new Locator.ClickOptions().setTimeout(2500));
            esperar(1000);
        } catch (Exception ignored) {
        }

        try {
            page.mouse().wheel(0, 1200);
            esperar(700);
            page.mouse().wheel(0, 1200);
            esperar(700);
            page.mouse().wheel(0, -1200);
            esperar(700);
        } catch (Exception ignored) {
        }
    }

    private boolean esUrlCategoriaValidaBershka(String href) {
        if (estaVacio(href)) {
            return false;
        }

        String url = href.toLowerCase();

        if (!url.contains("bershka.com")) {
            return false;
        }

        if (!url.contains("/es/")) {
            return false;
        }

        if (!url.contains("celement=") && !url.matches(".*-c\\d+\\.html.*")) {
            return false;
        }

        if (!url.contains("/mujer/") && !url.contains("/hombre/")) {
            return false;
        }

        if (url.contains("product") || url.contains("producto")) {
            return false;
        }

        if (url.contains("editorial")
                || url.contains("company")
                || url.contains("help")
                || url.contains("store-locator")
                || url.contains("privacy")
                || url.contains("cookies")) {
            return false;
        }

        return true;
    }

    private Long extraerIdCategoriaBershka(String href) {
        if (estaVacio(href)) {
            return null;
        }

        Pattern celementPattern = Pattern.compile("celement=(\\d+)");
        Matcher celementMatcher = celementPattern.matcher(href);

        if (celementMatcher.find()) {
            try {
                return Long.parseLong(celementMatcher.group(1));
            } catch (Exception ignored) {
            }
        }

        Pattern categoryPattern = Pattern.compile("-c(\\d+)\\.html");
        Matcher categoryMatcher = categoryPattern.matcher(href);

        if (categoryMatcher.find()) {
            try {
                return Long.parseLong(categoryMatcher.group(1));
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Seccion detectarSeccionDesdeUrl(String href) {
        String url = normalizarTexto(href);

        if (url.contains("MUJER")) {
            return Seccion.MUJER;
        }

        if (url.contains("HOMBRE")) {
            return Seccion.HOMBRE;
        }

        return Seccion.UNISEX;
    }

    private String detectarCategoriaDesdeUrlYTexto(String href, String texto) {
        String combinado = normalizarTexto(href + " " + texto);

        if (combinado.contains("JEANS")) {
            return "Jeans";
        }

        if (combinado.contains("VESTIDOS") || combinado.contains("MONOS")) {
            return "Vestidos";
        }

        if (combinado.contains("FALDAS")) {
            return "Faldas";
        }

        if (combinado.contains("PANTALONES") || combinado.contains("LEGGINGS") || combinado.contains("JOGGERS")) {
            return "Pantalones";
        }

        if (combinado.contains("SHORTS") || combinado.contains("BERMUDAS")) {
            return "Bermudas";
        }

        if (combinado.contains("CAMISAS") || combinado.contains("BLUSAS")) {
            return "Camisas";
        }

        if (combinado.contains("CAMISETAS") || combinado.contains("TOPS") || combinado.contains("BODIES")) {
            return "Camisetas";
        }

        if (combinado.contains("POLOS")) {
            return "Polos";
        }

        if (combinado.contains("CHAQUETAS")
                || combinado.contains("CAZADORAS")
                || combinado.contains("ABRIGOS")
                || combinado.contains("BLAZERS")
                || combinado.contains("AMERICANAS")
                || combinado.contains("CHALECOS")) {
            return "Chaquetas";
        }

        if (combinado.contains("SUDADERAS")) {
            return "Sudaderas";
        }

        if (combinado.contains("PUNTO")
                || combinado.contains("JERSEIS")
                || combinado.contains("JERSEYS")
                || combinado.contains("CARDIGAN")) {
            return "Punto";
        }

        if (combinado.contains("ZAPATOS")
                || combinado.contains("BOTAS")
                || combinado.contains("BOTINES")
                || combinado.contains("ZAPATILLAS")
                || combinado.contains("SANDALIAS")
                || combinado.contains("MOCASINES")) {
            return "Zapatos";
        }

        if (combinado.contains("BOLSOS")
                || combinado.contains("MOCHILAS")
                || combinado.contains("CARTERAS")
                || combinado.contains("NECESERES")) {
            return "Bolsos";
        }

        if (combinado.contains("ACCESORIOS")
                || combinado.contains("GORRAS")
                || combinado.contains("GORROS")
                || combinado.contains("CINTURONES")
                || combinado.contains("GAFAS")
                || combinado.contains("BISUTERIA")
                || combinado.contains("CALCETINES")
                || combinado.contains("BUFANDAS")
                || combinado.contains("PANUELOS")) {
            return "Accesorios";
        }

        if (combinado.contains("BANO")
                || combinado.contains("BIKINIS")
                || combinado.contains("BANADORES")) {
            return "Baño";
        }

        if (combinado.contains("ROPA")) {
            return "Ropa";
        }

        return "Otros";
    }

    private String convertirAUrlRelativaBershka(String href) {
        if (estaVacio(href)) {
            return "";
        }

        String url = href.trim();

        int indiceEs = url.indexOf("/es/");

        if (indiceEs != -1) {
            url = url.substring(indiceEs + 4);
        }

        while (url.startsWith("/")) {
            url = url.substring(1);
        }

        return url;
    }

    private boolean procesarCategoria(
            Page page,
            CategoriaBershka categoriaBershka,
            Map<String, Producto> productosGlobales,
            Tienda tienda
    ) {
        int productosNuevos = 0;
        int productosRepetidos = 0;
        int productosDescartados = 0;
        int productosSinImagen = 0;
        int productosSinPrecio = 0;

        System.out.println("====================================");
        System.out.println("PROCESANDO CATEGORÍA BERSHKA");
        System.out.println("====================================");
        System.out.println("Ruta: " + categoriaBershka.ruta());
        System.out.println("ID: " + categoriaBershka.id());
        System.out.println("Sección fallback: " + categoriaBershka.seccion());
        System.out.println("Categoría origen: " + categoriaBershka.categoria());
        System.out.println("Nombre categoría: " + categoriaBershka.nombre());

        try {
            List<String> productIds = obtenerProductIdsCategoria(page, categoriaBershka);

            System.out.println("IDs encontrados: " + productIds.size());

            if (productIds.isEmpty()) {
                System.out.println("Categoría sin productos.");
                System.out.println();
                return true;
            }

            List<String> productIdsPendientes = filtrarIdsNoProcesados(productIds, productosGlobales);

            System.out.println("IDs ya procesados antes de productsArray: " + (productIds.size() - productIdsPendientes.size()));
            System.out.println("IDs pendientes reales: " + productIdsPendientes.size());

            if (productIdsPendientes.isEmpty()) {
                System.out.println("Categoría sin productos nuevos. Se evita productsArray.");
                System.out.println();
                return true;
            }

            List<JsonNode> productosJson = cargarProductosCategoria(page, categoriaBershka, productIdsPendientes);

            System.out.println("Productos recibidos desde productsArray: " + productosJson.size());

            for (JsonNode productoJson : productosJson) {
                Producto producto = convertirJsonAProducto(productoJson, categoriaBershka, tienda);

                if (producto == null) {
                    productosDescartados++;
                    continue;
                }

                if (estaVacio(producto.getUrlImagen())) {
                    productosSinImagen++;
                }

                if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                    productosSinPrecio++;
                }

                String claveProducto = obtenerClaveProducto(productoJson, producto);

                if (estaVacio(claveProducto)) {
                    productosDescartados++;
                    continue;
                }

                if (productosGlobales.containsKey(claveProducto)) {
                    productosRepetidos++;
                    continue;
                }

                productosGlobales.put(claveProducto, producto);
                productosNuevos++;
            }

            System.out.println("OK categoría procesada.");
            System.out.println("Productos nuevos añadidos: " + productosNuevos);
            System.out.println("Productos repetidos ignorados: " + productosRepetidos);
            System.out.println("Productos descartados: " + productosDescartados);
            System.out.println("Productos sin imagen detectados: " + productosSinImagen);
            System.out.println("Productos sin precio detectados: " + productosSinPrecio);
            System.out.println();

            return true;

        } catch (Exception e) {
            System.out.println("Categoría saltada por error/bloqueo.");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println();
            e.printStackTrace();

            return false;
        }
    }

    private List<String> filtrarIdsNoProcesados(
            List<String> productIds,
            Map<String, Producto> productosGlobales
    ) {
        List<String> idsPendientes = new ArrayList<>();

        for (String id : productIds) {
            if (estaVacio(id)) {
                continue;
            }

            if (productosGlobales.containsKey(id)) {
                continue;
            }

            idsPendientes.add(id);
        }

        return idsPendientes;
    }

    private List<String> obtenerProductIdsCategoria(Page page, CategoriaBershka categoriaBershka) throws Exception {
        String endpoint = BASE_URL
                + "/itxrest/3"
                + "/catalog/store/" + STORE_ID
                + "/" + CATALOG_ID
                + "/category/" + categoriaBershka.id()
                + "/product?showProducts=false"
                + "&priceFilter=true"
                + "&showNoStock=false"
                + "&appId=1"
                + "&languageId=" + LANGUAGE_ID
                + "&locale=" + LOCALE;

        String referer = BASE_URL + "/es/" + categoriaBershka.url();

        JsonNode root = hacerPeticionConNavegador(page, endpoint, referer);

        Set<String> ids = new LinkedHashSet<>();

        JsonNode productIdsNode = root.path("productIds");
        if (productIdsNode.isArray()) {
            for (JsonNode idNode : productIdsNode) {
                String id = idNode.asText("");
                if (!estaVacio(id)) {
                    ids.add(id);
                }
            }
        }

        JsonNode sortedProductIdsNode = root.path("sortedProductIds");
        if (sortedProductIdsNode.isArray()) {
            for (JsonNode idNode : sortedProductIdsNode) {
                String id = idNode.asText("");
                if (!estaVacio(id)) {
                    ids.add(id);
                }
            }
        }

        JsonNode gridElements = root.path("gridElements");
        if (gridElements.isArray()) {
            for (JsonNode gridElement : gridElements) {
                JsonNode ccIds = gridElement.path("ccIds");

                if (ccIds.isArray()) {
                    for (JsonNode ccIdNode : ccIds) {
                        String id = ccIdNode.asText("");
                        if (!estaVacio(id)) {
                            ids.add(id);
                        }
                    }
                }

                JsonNode commercialComponentIds = gridElement.path("commercialComponentIds");

                if (commercialComponentIds.isArray()) {
                    for (JsonNode component : commercialComponentIds) {
                        String tipo = texto(component, "type");
                        String kind = texto(component, "kind");
                        String id = texto(component, "ccId");

                        if (!estaVacio(id)
                                && !"Bundle".equalsIgnoreCase(tipo)
                                && !"Marketing".equalsIgnoreCase(kind)) {
                            ids.add(id);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(ids);
    }

    private List<JsonNode> cargarProductosCategoria(
            Page page,
            CategoriaBershka categoriaBershka,
            List<String> productIds
    ) throws Exception {
        List<JsonNode> productos = new ArrayList<>();
        List<List<String>> bloques = dividirEnBloques(productIds, TAMANO_BLOQUE_PRODUCTOS);

        System.out.println("Bloques productsArray a cargar: " + bloques.size());

        for (List<String> bloque : bloques) {
            String idsTexto = String.join(",", bloque);

            String endpoint = BASE_URL
                    + "/itxrest/3"
                    + "/catalog/store/" + STORE_ID
                    + "/" + CATALOG_ID
                    + "/productsArray?languageId=" + LANGUAGE_ID
                    + "&productIds=" + idsTexto
                    + "&appId=1"
                    + "&locale=" + LOCALE;

            String referer = BASE_URL + "/es/" + categoriaBershka.url();

            JsonNode root = hacerPeticionConNavegador(page, endpoint, referer);
            JsonNode productsNode = root.path("products");

            if (!productsNode.isArray()) {
                esperar(PAUSA_ENTRE_BLOQUES_MS);
                continue;
            }

            for (JsonNode productoNode : productsNode) {
                productos.add(productoNode);
            }

            esperar(PAUSA_ENTRE_BLOQUES_MS);
        }

        return productos;
    }

    private JsonNode hacerPeticionConNavegador(Page page, String endpoint, String referer) throws Exception {
        RespuestaFetch respuesta = fetchDesdeNavegador(page, endpoint, referer);

        if (respuesta.statusCode() == 403) {
            System.out.println("403 detectado. Reintentando una vez desde la URL de categoría...");

            try {
                page.navigate(referer, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(60000)
                );

                esperar(PAUSA_REINTENTO_403_MS);
                aceptarCookiesSiAparece(page);
            } catch (Exception e) {
                System.out.println("No se pudo navegar al referer.");
                System.out.println("Mensaje navegación: " + e.getMessage());
            }

            respuesta = fetchDesdeNavegador(page, endpoint, referer);
        }

        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
            System.out.println("====================================");
            System.out.println("RESPUESTA ERROR BERSHKA");
            System.out.println("====================================");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Referer: " + referer);
            System.out.println("Status: " + respuesta.statusCode());
            System.out.println("Body corto:");
            imprimirBodyCorto(respuesta.body());
            System.out.println("====================================");

            throw new RuntimeException("Respuesta HTTP no válida: " + respuesta.statusCode());
        }

        return objectMapper.readTree(respuesta.body());
    }

    private RespuestaFetch fetchDesdeNavegador(Page page, String endpoint, String referer) throws Exception {
        String script = """
                async (args) => {
                    const res = await fetch(args.url, {
                        method: "GET",
                        credentials: "include",
                        cache: "no-store",
                        referrer: args.referer,
                        headers: {
                            "accept": "application/json, text/plain, */*",
                            "accept-language": "es-ES,es;q=0.9",
                            "cache-control": "no-cache",
                            "pragma": "no-cache"
                        }
                    });

                    const text = await res.text();

                    return JSON.stringify({
                        status: res.status,
                        length: text.length,
                        body: text
                    });
                }
                """;

        Object resultado = page.evaluate(script, Map.of(
                "url", endpoint,
                "referer", referer
        ));

        JsonNode respuestaJson = objectMapper.readTree(String.valueOf(resultado));

        int status = respuestaJson.path("status").asInt();
        int length = respuestaJson.path("length").asInt();
        String body = respuestaJson.path("body").asText("");

        System.out.println("FETCH navegador status: " + status + " | longitud: " + length);

        return new RespuestaFetch(status, body);
    }

    private Producto convertirJsonAProducto(
            JsonNode productoJson,
            CategoriaBershka categoriaBershka,
            Tienda tienda
    ) {
        String id = texto(productoJson, "id");
        String nombre = texto(productoJson, "name");
        String productUrl = texto(productoJson, "productUrl");
        String productUrlParam = texto(productoJson, "productUrlParam");

        String mainColorId = texto(productoJson, "mainColorid");

        if (estaVacio(mainColorId)) {
            mainColorId = texto(productoJson, "mainColorId");
        }

        if (estaVacio(id) || estaVacio(nombre)) {
            return null;
        }

        JsonNode resumen = primerElemento(productoJson.path("bundleProductSummaries"));
        JsonNode detail = resumen.path("detail");

        if (detail.isMissingNode() || detail.isNull() || detail.size() == 0) {
            detail = productoJson.path("detail");
        }

        String descripcion = texto(detail, "longDescription");

        if (estaVacio(descripcion)) {
            descripcion = texto(detail, "description");
        }

        BigDecimal precio = obtenerPrecio(detail);

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            precio = convertirPrecio(texto(productoJson, "price"));
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String colorId = obtenerColorId(productoJson, detail, mainColorId);
        String urlProducto = crearUrlProducto(productUrl, colorId, productUrlParam, id);

        if (estaVacio(urlProducto)) {
            return null;
        }

        List<String> imagenesExtraidas = extraerImagenesProducto(detail);

        String imagenPrincipal = imagenesExtraidas.isEmpty()
                ? ""
                : imagenesExtraidas.get(0);

        String familyName = texto(productoJson, "familyName");
        String subFamilyName = texto(productoJson, "subFamilyName");
        String sectionNameEN = texto(productoJson, "sectionNameEN");
        String sectionName = texto(productoJson, "sectionName");

        Seccion seccion = convertirSeccionBershka(sectionNameEN, sectionName, categoriaBershka.seccion());

        String nombreCategoria = normalizarCategoriaBershka(
                nombre,
                familyName,
                subFamilyName,
                categoriaBershka.categoria(),
                categoriaBershka.nombre()
        );

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreCategoria);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setUrlImagen(imagenPrincipal);
        producto.setUrlProducto(urlProducto);
        producto.setSeccion(seccion);
        producto.setCategoria(categoria);
        producto.setTienda(tienda);

        int orden = 1;

        for (String urlImagen : imagenesExtraidas) {
            ProductoImagen productoImagen = new ProductoImagen();
            productoImagen.setUrlImagen(urlImagen);
            productoImagen.setOrden(orden);

            producto.addImagen(productoImagen);

            orden++;
        }

        return producto;
    }

    private String normalizarCategoriaBershka(
            String nombreProducto,
            String familyName,
            String subFamilyName,
            String categoriaOrigen,
            String nombreCategoriaMenu
    ) {
        String padre = normalizarCategoriaPadreBershka(categoriaOrigen);

        if (!esCategoriaMixtaBershka(categoriaOrigen)) {
            return padre;
        }

        return normalizarCategoria(
                nombreProducto,
                familyName,
                subFamilyName,
                categoriaOrigen,
                nombreCategoriaMenu
        );
    }

    private String normalizarCategoriaPadreBershka(String categoriaPadre) {
        String origen = normalizarTexto(categoriaPadre);

        if (origen.contains("JEANS")) {
            return "Jeans";
        }

        if (origen.contains("VESTIDOS") || origen.contains("MONOS")) {
            return "Vestidos";
        }

        if (origen.contains("FALDAS")) {
            return "Faldas";
        }

        if (origen.contains("PANTALONES")) {
            return "Pantalones";
        }

        if (origen.contains("BERMUDAS") || origen.contains("SHORTS")) {
            return "Bermudas";
        }

        if (origen.contains("CAMISAS") || origen.contains("BLUSAS")) {
            return "Camisas";
        }

        if (origen.contains("CAMISETAS") || origen.contains("TOPS") || origen.contains("BODIES")) {
            return "Camisetas";
        }

        if (origen.contains("POLOS")) {
            return "Polos";
        }

        if (origen.contains("CAZADORAS")
                || origen.contains("GABARDINAS")
                || origen.contains("BLAZERS")
                || origen.contains("CHAQUETAS")
                || origen.contains("ABRIGOS")
                || origen.contains("AMERICANAS")) {
            return "Chaquetas";
        }

        if (origen.contains("SUDADERAS")) {
            return "Sudaderas";
        }

        if (origen.contains("PUNTO") || origen.contains("JERSEIS") || origen.contains("JERSEYS")) {
            return "Punto";
        }

        if (origen.contains("ZAPATOS")) {
            return "Zapatos";
        }

        if (origen.contains("BOLSOS") || origen.contains("MOCHILAS")) {
            return "Bolsos";
        }

        if (origen.contains("ACCESORIOS")) {
            return "Accesorios";
        }

        if (origen.contains("BANO")) {
            return "Baño";
        }

        return "Otros";
    }

    private boolean esCategoriaMixtaBershka(String categoriaPadre) {
        String origen = normalizarTexto(categoriaPadre);

        return origen.contains("ROPA")
                || origen.contains("BASICOS")
                || origen.contains("NOVEDADES")
                || origen.contains("REBAJAS")
                || origen.contains("TOTAL LOOK")
                || origen.contains("OTROS");
    }

    private String obtenerClaveProducto(JsonNode productoJson, Producto producto) {
        if (producto != null && !estaVacio(producto.getUrlProducto())) {
            return limpiarUrlProductoBershka(producto.getUrlProducto());
        }

        String productUrl = texto(productoJson, "productUrl");

        if (!estaVacio(productUrl)) {
            return limpiarUrlProductoBershka(productUrl);
        }

        String id = texto(productoJson, "id");

        if (!estaVacio(id)) {
            return id;
        }

        return "";
    }

    private String limpiarUrlProductoBershka(String urlProducto) {
        if (estaVacio(urlProducto)) {
            return "";
        }

        String urlLimpia = urlProducto.trim();

        int indiceParametros = urlLimpia.indexOf("?");

        if (indiceParametros != -1) {
            urlLimpia = urlLimpia.substring(0, indiceParametros);
        }

        return urlLimpia;
    }

    private BigDecimal obtenerPrecio(JsonNode detail) {
        JsonNode colors = detail.path("colors");

        if (!colors.isArray()) {
            return null;
        }

        for (JsonNode color : colors) {
            JsonNode sizes = color.path("sizes");

            if (!sizes.isArray()) {
                continue;
            }

            for (JsonNode size : sizes) {
                String precioTexto = texto(size, "price");

                if (!estaVacio(precioTexto)) {
                    BigDecimal precio = convertirPrecio(precioTexto);

                    if (precio != null && precio.compareTo(BigDecimal.ZERO) > 0) {
                        return precio;
                    }
                }
            }
        }

        return null;
    }

    private String obtenerColorId(JsonNode productoJson, JsonNode detail, String mainColorId) {
        if (!estaVacio(mainColorId)) {
            return mainColorId;
        }

        JsonNode colors = detail.path("colors");

        if (colors.isArray() && colors.size() > 0) {
            String colorId = texto(colors.get(0), "id");

            if (!estaVacio(colorId)) {
                return colorId;
            }
        }

        JsonNode bundleColors = productoJson.path("bundleColors");

        if (bundleColors.isArray() && bundleColors.size() > 0) {
            String colorId = texto(bundleColors.get(0), "id");

            if (!estaVacio(colorId)) {
                return colorId;
            }
        }

        return "";
    }

    private String crearUrlProducto(
            String productUrl,
            String colorId,
            String productUrlParam,
            String id
    ) {
        if (estaVacio(productUrl) || estaVacio(colorId)) {
            return "";
        }

        String pelement = !estaVacio(productUrlParam)
                ? productUrlParam
                : id;

        if (estaVacio(pelement)) {
            return "";
        }

        String urlLimpia = productUrl.trim();

        if (urlLimpia.startsWith("http://") || urlLimpia.startsWith("https://")) {
            String separador = urlLimpia.contains("?") ? "&" : "?";
            return urlLimpia + separador + "cS=" + colorId + "&pelement=" + pelement;
        }

        while (urlLimpia.startsWith("/")) {
            urlLimpia = urlLimpia.substring(1);
        }

        if (urlLimpia.startsWith("es/")) {
            urlLimpia = urlLimpia.substring(3);
        }

        return BASE_URL + "/es/" + urlLimpia + "?cS=" + colorId + "&pelement=" + pelement;
    }

    private List<String> extraerImagenesProducto(JsonNode detail) {
        List<String> imagenes = new ArrayList<>();
        Set<String> imagenesVistas = new HashSet<>();

        JsonNode xmedia = detail.path("xmedia");

        if (!xmedia.isArray()) {
            return imagenes;
        }

        for (JsonNode bloque : xmedia) {
            JsonNode xmediaItems = bloque.path("xmediaItems");

            if (!xmediaItems.isArray()) {
                continue;
            }

            for (JsonNode item : xmediaItems) {
                JsonNode medias = item.path("medias");

                if (!medias.isArray()) {
                    continue;
                }

                for (JsonNode media : medias) {
                    String urlImagen = obtenerUrlImagen(media);

                    if (!esImagenValida(urlImagen)) {
                        continue;
                    }

                    if (imagenesVistas.contains(urlImagen)) {
                        continue;
                    }

                    imagenesVistas.add(urlImagen);
                    imagenes.add(urlImagen);

                    if (imagenes.size() >= MAX_IMAGENES_POR_PRODUCTO) {
                        return imagenes;
                    }
                }
            }
        }

        return imagenes;
    }

    private String obtenerUrlImagen(JsonNode media) {
        String deliveryUrl = texto(media.path("extraInfo"), "deliveryUrl");

        if (!estaVacio(deliveryUrl)) {
            return deliveryUrl;
        }

        String extraInfoUrl = texto(media.path("extraInfo"), "url");

        if (!estaVacio(extraInfoUrl)) {
            return extraInfoUrl;
        }

        return texto(media, "url");
    }

    private boolean esImagenValida(String urlImagen) {
        if (estaVacio(urlImagen)) {
            return false;
        }

        String urlNormalizada = urlImagen.toLowerCase();

        if (urlNormalizada.contains("color_")) {
            return false;
        }

        if (urlNormalizada.contains("meta.json")) {
            return false;
        }

        return urlNormalizada.contains(".jpg")
                || urlNormalizada.contains(".jpeg")
                || urlNormalizada.contains(".png")
                || urlNormalizada.contains(".webp");
    }

    private Seccion convertirSeccionBershka(String sectionNameEN, String sectionName, Seccion seccionFallback) {
        String seccionNormalizada = normalizarTexto(sectionNameEN + " " + sectionName);

        if (seccionNormalizada.contains("WOMEN") || seccionNormalizada.contains("WOMAN") || seccionNormalizada.contains("MUJER")) {
            return Seccion.MUJER;
        }

        if (seccionNormalizada.contains("MEN") || seccionNormalizada.contains("MAN") || seccionNormalizada.contains("HOMBRE")) {
            return Seccion.HOMBRE;
        }

        return seccionFallback;
    }

    private String normalizarCategoria(
            String nombreProducto,
            String familyName,
            String subFamilyName,
            String categoriaOrigen,
            String nombreCategoriaMenu
    ) {
        String nombre = normalizarTexto(nombreProducto);
        String familia = normalizarTexto(familyName);
        String subfamilia = normalizarTexto(subFamilyName);
        String origen = normalizarTexto(categoriaOrigen);
        String menu = normalizarTexto(nombreCategoriaMenu);
        String textoCompleto = unirTextos(nombre, familia, subfamilia, origen, menu);

        if (empiezaPorAlgunaPalabra(nombre, "VESTIDO", "MONO")) {
            return "Vestidos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "FALDA")) {
            return "Faldas";
        }

        if (esZapato(textoCompleto)) {
            return "Zapatos";
        }

        if (esBolso(textoCompleto)) {
            return "Bolsos";
        }

        if (esAccesorio(textoCompleto)) {
            return "Accesorios";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CAMISA", "SOBRECAMISA", "BLUSA")) {
            return "Camisas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "POLO")) {
            return "Polos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CAMISETA", "TOP", "BODY", "CORSET")) {
            return "Camisetas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CHAQUETA", "CAZADORA", "BOMBER", "BLAZER", "GABARDINA", "TRENCH", "ABRIGO", "CHALECO")) {
            return "Chaquetas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "SUDADERA")) {
            return "Sudaderas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "JERSEY", "CARDIGAN", "CÁRDIGAN")) {
            return "Punto";
        }

        if (empiezaPorAlgunaPalabra(nombre, "BIKINI", "BAÑADOR", "BANADOR")) {
            return "Baño";
        }

        if (empiezaPorAlgunaPalabra(nombre, "PANTALON", "PANTALÓN", "LEGGING", "LEGGINGS", "JOGGER")) {
            if (contieneAlgunaPalabra(textoCompleto, "DENIM", "JEANS", "VAQUERO", "VAQUERA")) {
                return "Jeans";
            }

            return "Pantalones";
        }

        if (empiezaPorAlgunaPalabra(nombre, "BERMUDA", "SHORT", "SHORTS", "JORTS")) {
            if (contieneAlgunaPalabra(textoCompleto, "DENIM", "JEANS", "VAQUERO", "VAQUERA")) {
                return "Jeans";
            }

            return "Bermudas";
        }

        if (contieneAlgunaPalabra(familia, "VESTIDO", "MONO")) {
            return "Vestidos";
        }

        if (contieneAlgunaPalabra(familia, "FALDA")) {
            return "Faldas";
        }

        if (contieneAlgunaPalabra(familia, "CAMISA", "BLUSA")) {
            return "Camisas";
        }

        if (contieneAlgunaPalabra(familia, "POLO")) {
            return "Polos";
        }

        if (contieneAlgunaPalabra(familia, "CAMISETA", "TOP", "BODY")) {
            return "Camisetas";
        }

        if (contieneAlgunaPalabra(familia, "SUDADERA")) {
            return "Sudaderas";
        }

        if (contieneAlgunaPalabra(familia, "JERSEY", "PUNTO", "CARDIGAN")) {
            return "Punto";
        }

        if (contieneAlgunaPalabra(familia, "PANTALON", "LEGGING", "JOGGER")) {
            if (contieneAlgunaPalabra(textoCompleto, "DENIM", "JEANS", "VAQUERO", "VAQUERA")) {
                return "Jeans";
            }

            return "Pantalones";
        }

        if (contieneAlgunaPalabra(familia, "BERMUDA", "SHORT")) {
            if (contieneAlgunaPalabra(textoCompleto, "DENIM", "JEANS", "VAQUERO", "VAQUERA")) {
                return "Jeans";
            }

            return "Bermudas";
        }

        if (origen.contains("JEANS")) {
            return "Jeans";
        }

        if (origen.contains("VESTIDOS")) {
            return "Vestidos";
        }

        if (origen.contains("FALDAS")) {
            return "Faldas";
        }

        if (origen.contains("BERMUDAS") || origen.contains("SHORTS")) {
            return "Bermudas";
        }

        if (origen.contains("PANTALONES")) {
            return "Pantalones";
        }

        if (origen.contains("CAMISAS") || origen.contains("BLUSAS")) {
            return "Camisas";
        }

        if (origen.contains("POLOS")) {
            return "Polos";
        }

        if (origen.contains("CAMISETAS") || origen.contains("TOPS")) {
            return "Camisetas";
        }

        if (origen.contains("CAZADORAS")
                || origen.contains("GABARDINAS")
                || origen.contains("BLAZERS")
                || origen.contains("CHAQUETAS")
                || origen.contains("ABRIGOS")) {
            return "Chaquetas";
        }

        if (origen.contains("SUDADERAS")) {
            return "Sudaderas";
        }

        if (origen.contains("PUNTO")) {
            return "Punto";
        }

        if (origen.contains("BANO")) {
            return "Baño";
        }

        if (origen.contains("CHANDAL")) {
            return "Chándal";
        }

        if (origen.contains("ZAPATOS")) {
            return "Zapatos";
        }

        if (origen.contains("BOLSOS") || origen.contains("MOCHILAS")) {
            return "Bolsos";
        }

        if (origen.contains("ACCESORIOS")) {
            return "Accesorios";
        }

        return "Otros";
    }

    private boolean esZapato(String texto) {
        return contieneAlgunaPalabra(texto,
                "ZAPATO", "ZAPATILLA", "BOTA", "BOTIN", "BOTÍN",
                "SANDALIA", "MOCASIN", "MOCASÍN", "ALPARGATA",
                "BAILARINA", "MULE", "TACON", "TACÓN", "RUNNING",
                "CALZADO"
        );
    }

    private boolean esBolso(String texto) {
        return contieneAlgunaPalabra(texto,
                "BOLSO", "MOCHILA", "CARTERA", "MONEDERO",
                "RIÑONERA", "RINONERA", "NECESER", "SHOPPER",
                "BANDOLERA"
        );
    }

    private boolean esAccesorio(String texto) {
        return contieneAlgunaPalabra(texto,
                "ACCESORIO", "CINTURON", "CINTURÓN", "GORRA",
                "GORRO", "SOMBRERO", "PAÑUELO", "PANUELO",
                "BUFANDA", "GAFAS", "LLAVERO", "COLLAR",
                "PENDIENTE", "PULSERA", "ANILLO", "CALCETIN",
                "CALCETÍN", "BISUTERIA", "BISUTERÍA"
        );
    }

    private List<List<String>> dividirEnBloques(List<String> elementos, int tamanoBloque) {
        List<List<String>> bloques = new ArrayList<>();

        for (int i = 0; i < elementos.size(); i += tamanoBloque) {
            int fin = Math.min(i + tamanoBloque, elementos.size());
            bloques.add(elementos.subList(i, fin));
        }

        return bloques;
    }

    private BigDecimal convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.isBlank()) {
            return null;
        }

        try {
            BigDecimal precioCentimos = new BigDecimal(precioTexto.trim());
            return precioCentimos.divide(BigDecimal.valueOf(100));
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode primerElemento(JsonNode array) {
        if (array != null && array.isArray() && array.size() > 0) {
            return array.get(0);
        }

        return objectMapper.createObjectNode();
    }

    private String texto(JsonNode nodo, String campo) {
        if (nodo == null || nodo.isMissingNode() || nodo.isNull()) {
            return "";
        }

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return "";
        }

        return valor.asText("");
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace("Ñ", "N")
                .replaceAll("[^A-Z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean empiezaPorAlgunaPalabra(String texto, String... palabras) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        for (String palabra : palabras) {
            String palabraNormalizada = normalizarTexto(palabra);

            if (texto.equals(palabraNormalizada) || texto.startsWith(palabraNormalizada + " ")) {
                return true;
            }
        }

        return false;
    }

    private boolean contieneAlgunaPalabra(String texto, String... palabras) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        String textoNormalizado = normalizarTexto(texto);
        String textoConEspacios = " " + textoNormalizado + " ";

        for (String palabra : palabras) {
            String palabraNormalizada = normalizarTexto(palabra);

            if (textoConEspacios.contains(" " + palabraNormalizada + " ")) {
                return true;
            }
        }

        return false;
    }

    private String unirTextos(String... textos) {
        StringBuilder resultado = new StringBuilder();

        for (String texto : textos) {
            if (texto != null && !texto.isBlank()) {
                resultado.append(texto).append(" ");
            }
        }

        return resultado.toString().trim();
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private void esperar(int milisegundos) {
        if (milisegundos <= 0) {
            return;
        }

        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void imprimirBodyCorto(String body) {
        if (body == null) {
            System.out.println("");
            return;
        }

        int limite = Math.min(body.length(), 700);
        System.out.println(body.substring(0, limite));
    }

    private String userAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/147.0.0.0 Safari/537.36";
    }

    private void imprimirResumenFinal(List<Producto> productos, int categoriasOk, int categoriasFallidas) {
        long productosSinImagen = productos.stream()
                .filter(producto -> estaVacio(producto.getUrlImagen()))
                .count();

        long productosSinPrecio = productos.stream()
                .filter(producto -> producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0)
                .count();

        long productosSinUrl = productos.stream()
                .filter(producto -> estaVacio(producto.getUrlProducto()))
                .count();

        long totalImagenesExtraidas = productos.stream()
                .filter(producto -> producto.getImagenes() != null)
                .mapToLong(producto -> producto.getImagenes().size())
                .sum();

        Map<String, Long> conteoPorCategoria = new LinkedHashMap<>();
        Map<Seccion, Long> conteoPorSeccion = new LinkedHashMap<>();

        for (Producto producto : productos) {
            String categoria = producto.getCategoria() != null
                    ? producto.getCategoria().getNombre()
                    : "Sin categoría";

            conteoPorCategoria.put(categoria, conteoPorCategoria.getOrDefault(categoria, 0L) + 1);

            Seccion seccion = producto.getSeccion() != null
                    ? producto.getSeccion()
                    : Seccion.UNISEX;

            conteoPorSeccion.put(seccion, conteoPorSeccion.getOrDefault(seccion, 0L) + 1);
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println("RESUMEN FINAL BERSHKA");
        System.out.println("====================================");
        System.out.println("Categorías OK: " + categoriasOk);
        System.out.println("Categorías fallidas/bloqueadas: " + categoriasFallidas);
        System.out.println("Productos únicos finales: " + productos.size());
        System.out.println("Productos sin imagen principal: " + productosSinImagen);
        System.out.println("Productos sin precio: " + productosSinPrecio);
        System.out.println("Productos sin URL: " + productosSinUrl);
        System.out.println("Total imágenes extraídas: " + totalImagenesExtraidas);

        System.out.println();
        System.out.println("====================================");
        System.out.println("PRODUCTOS POR SECCIÓN");
        System.out.println("====================================");

        conteoPorSeccion.forEach((seccion, total) ->
                System.out.println(seccion + ": " + total)
        );

        System.out.println();
        System.out.println("====================================");
        System.out.println("PRODUCTOS POR CATEGORÍA");
        System.out.println("====================================");

        conteoPorCategoria.forEach((categoria, total) ->
                System.out.println(categoria + ": " + total)
        );

        System.out.println();
        System.out.println("====================================");
        System.out.println("PRIMEROS 30 PRODUCTOS");
        System.out.println("====================================");

        productos.stream()
                .limit(30)
                .forEach(producto -> {
                    int totalImagenesProducto = producto.getImagenes() != null
                            ? producto.getImagenes().size()
                            : 0;

                    System.out.println("------------------------------------");
                    System.out.println("Nombre: " + producto.getNombre());
                    System.out.println("Precio: " + producto.getPrecio());
                    System.out.println("Sección: " + producto.getSeccion());
                    System.out.println("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria().getNombre() : ""));
                    System.out.println("URL: " + producto.getUrlProducto());
                    System.out.println("Imagen principal: " + producto.getUrlImagen());
                    System.out.println("Total imágenes: " + totalImagenesProducto);
                });

        System.out.println();
        System.out.println("SCRAPING BERSHKA TERMINADO.");
    }

    private record CategoriaBershka(
            long id,
            Seccion seccion,
            String categoria,
            String nombre,
            String url,
            String ruta
    ) {
    }

    private record RespuestaFetch(
            int statusCode,
            String body
    ) {
    }
}