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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PullAndBearScraper implements ScraperTienda {

    private static final String BASE_URL = "https://www.pullandbear.com";
    private static final String STORE_ID = "24009400";
    private static final String CATALOG_ID = "20309449";
    private static final String LANGUAGE_ID = "-5";

    private static final int TAMANO_BLOQUE_PRODUCTOS = 40;
    private static final int PAUSA_ENTRE_BLOQUES_MS = 0;
    private static final int PAUSA_ENTRE_CATEGORIAS_MS = 0;
    private static final int PAUSA_REINTENTO_403_MS = 800;

    private static final boolean HEADLESS = true;
    private static final boolean MODO_UNA_CATEGORIA_DEBUG = false;

    private static final int MAX_IMAGENES_POR_PRODUCTO = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getNombreTienda() {
        return "PullAndBear";
    }

    @Override
    public List<Producto> scrapearProductos() {
        List<CategoriaPullBear> categoriasPullBear = MODO_UNA_CATEGORIA_DEBUG
                ? obtenerCategoriaDebug()
                : obtenerCategoriasPullBear();

        Map<String, Producto> productosGlobales = new LinkedHashMap<>();

        Tienda tienda = new Tienda();
        tienda.setNombre("PullAndBear");
        tienda.setUrl(BASE_URL);

        int categoriasOk = 0;
        int categoriasFallidas = 0;

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

            for (CategoriaPullBear categoriaPullBear : categoriasPullBear) {
                boolean categoriaProcesada = procesarCategoria(page, categoriaPullBear, productosGlobales, tienda);

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
        }

        List<Producto> productosFinales = new ArrayList<>(productosGlobales.values());

        imprimirResumenFinal(productosFinales, categoriasOk, categoriasFallidas);

        return productosFinales;
    }

    private Browser lanzarNavegador(Playwright playwright) {
        try {
            return playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("chrome")
                    .setHeadless(HEADLESS)
            );
        } catch (PlaywrightException e) {
            return playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(HEADLESS)
            );
        }
    }

    private void prepararPaginaInicial(Page page) {
        try {
            page.navigate(BASE_URL + "/es/", new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(60000)
            );

            esperar(3000);
            aceptarCookiesSiAparece(page);

        } catch (Exception e) {
        }
    }

    private void aceptarCookiesSiAparece(Page page) {
        try {
            page.locator("button:has-text('Aceptar')").first().click(new Locator.ClickOptions().setTimeout(2500));
            esperar(1000);
        } catch (Exception ignored) {
        }
    }

    private List<CategoriaPullBear> obtenerCategoriaDebug() {
        return List.of(
                new CategoriaPullBear(
                        1030017536L,
                        Seccion.MUJER,
                        "Nueva colección",
                        "Novedades mujer",
                        "mujer/novedades-n6491",
                        "TEST > Mujer > Nueva colección > Novedades"
                )
        );
    }

    private List<CategoriaPullBear> obtenerCategoriasPullBear() {
        return List.of(
                new CategoriaPullBear(1030017536L, Seccion.MUJER, "Nueva colección", "Novedades mujer", "mujer/novedades-n6491", "Mujer > Nueva colección > Novedades"),
                new CategoriaPullBear(1030017537L, Seccion.HOMBRE, "Nueva colección", "Novedades hombre", "hombre/novedades-n6280", "Hombre > Nueva colección > Novedades"),

                new CategoriaPullBear(1030204693L, Seccion.MUJER, "Jeans", "Ver todo", "mujer/ropa/jeans-n6581", "Mujer > Colección > Jeans > Ver todo"),
                new CategoriaPullBear(1030526548L, Seccion.MUJER, "Jeans", "Baggy | Barrel", "mujer/ropa/jeans/baggy-n7649", "Mujer > Colección > Jeans > Baggy | Barrel"),

                new CategoriaPullBear(1030207192L, Seccion.MUJER, "Pantalones", "Ver todo", "mujer/ropa/pantalones-n6600", "Mujer > Colección > Pantalones > Ver todo"),
                new CategoriaPullBear(1030318057L, Seccion.MUJER, "Pantalones", "De vestir", "mujer/ropa/pantalones/de-vestir-n7033", "Mujer > Colección > Pantalones > De vestir"),
                new CategoriaPullBear(1030207191L, Seccion.MUJER, "Pantalones", "Jogging", "mujer/ropa/pantalones/joggers-n6904", "Mujer > Colección > Pantalones > Jogging"),
                new CategoriaPullBear(1030719220L, Seccion.MUJER, "Pantalones", "Bombacho", "mujer/ropa/pantalones/bombacho-n7976", "Mujer > Colección > Pantalones > Bombacho"),

                new CategoriaPullBear(1030422324L, Seccion.MUJER, "Tops | Bodies", "Ver todo", "mujer/ropa/tops-n6644", "Mujer > Colección > Tops | Bodies > Ver todo"),

                new CategoriaPullBear(1030204632L, Seccion.MUJER, "Camisetas", "Ver todo", "mujer/ropa/camisetas-n6541", "Mujer > Colección > Camisetas > Ver todo"),
                new CategoriaPullBear(1030204636L, Seccion.MUJER, "Camisetas", "Básicas", "mujer/ropa/basicos/camisetas-n6516", "Mujer > Colección > Camisetas > Básicas"),
                new CategoriaPullBear(1030204633L, Seccion.MUJER, "Camisetas", "Manga corta", "mujer/ropa/camisetas/manga-corta-n6547", "Mujer > Colección > Camisetas > Manga corta"),
                new CategoriaPullBear(1030204634L, Seccion.MUJER, "Camisetas", "Manga larga", "mujer/ropa/camisetas/manga-larga-n6548", "Mujer > Colección > Camisetas > Manga larga"),
                new CategoriaPullBear(1030204641L, Seccion.MUJER, "Camisetas", "Rayas", "mujer/ropa/camisetas/rayas-n6550", "Mujer > Colección > Camisetas > Rayas"),
                new CategoriaPullBear(1030204637L, Seccion.MUJER, "Camisetas", "Graficas", "mujer/ropa/camisetas/estampadas-n6545", "Mujer > Colección > Camisetas > Graficas"),

                new CategoriaPullBear(1030204617L, Seccion.MUJER, "Vestidos", "Ver todo", "mujer/ropa/vestidos-n6646", "Mujer > Colección > Vestidos > Ver todo"),

                new CategoriaPullBear(1030204608L, Seccion.MUJER, "Cazadoras | Gabardinas", "Ver todo", "mujer/ropa/cazadoras-y-chaquetas-n6555", "Mujer > Colección > Cazadoras | Gabardinas > Ver todo"),
                new CategoriaPullBear(1030717216L, Seccion.MUJER, "Cazadoras | Gabardinas", "Globo", "mujer/ropa/chaquetas/globo-n7967", "Mujer > Colección > Cazadoras | Gabardinas > Globo"),

                new CategoriaPullBear(1030543096L, Seccion.MUJER, "Blazers", "Blazers", "mujer/ropa/cazadoras-y-chaquetas/blazers-y-americanas-n6558", "Mujer > Colección > Blazers > Blazers"),
                new CategoriaPullBear(1030543597L, Seccion.MUJER, "Blazers", "Ver todo", "mujer/ropa/trajes-n7305", "Mujer > Colección > Blazers > Ver todo"),

                new CategoriaPullBear(1030204645L, Seccion.MUJER, "Camisas | Blusas", "Camisas | Blusas", "mujer/ropa/blusas-y-camisas-n6525", "Mujer > Colección > Camisas | Blusas"),

                new CategoriaPullBear(1030204679L, Seccion.MUJER, "Faldas", "Ver todo", "mujer/ropa/faldas-n6571", "Mujer > Colección > Faldas > Ver todo"),

                new CategoriaPullBear(1030204686L, Seccion.MUJER, "Shorts | Bermudas", "Ver todo", "mujer/ropa/shorts-n6629", "Mujer > Colección > Shorts | Bermudas > Ver todo"),

                new CategoriaPullBear(1030204661L, Seccion.MUJER, "Sudaderas", "Ver todo", "mujer/ropa/sudaderas-n6636", "Mujer > Colección > Sudaderas > Ver todo"),
                new CategoriaPullBear(1030204662L, Seccion.MUJER, "Sudaderas", "Básicas", "mujer/ropa/basicos/sudaderas-n6521", "Mujer > Colección > Sudaderas > Básicas"),

                new CategoriaPullBear(1030204670L, Seccion.MUJER, "Punto", "Ver todo", "mujer/ropa/punto-n6618", "Mujer > Colección > Punto > Ver todo"),

                new CategoriaPullBear(1030275496L, Seccion.MUJER, "Total look", "Total look", "mujer/ropa/twin-sets-n6987", "Mujer > Colección > Total look"),
                new CategoriaPullBear(29025L, Seccion.MUJER, "Básicos", "Básicos", "mujer/ropa/basicos-n6514", "Mujer > Colección > Básicos"),

                new CategoriaPullBear(1030207001L, Seccion.MUJER, "Zapatos", "Ver todo", "mujer/zapatos-n6685", "Mujer > Colección > Zapatos > Ver todo"),
                new CategoriaPullBear(739503L, Seccion.MUJER, "Zapatos", "Novedades", "mujer/novedades/zapatos-n6836", "Mujer > Colección > Zapatos > Novedades"),
                new CategoriaPullBear(1030312008L, Seccion.MUJER, "Zapatos", "Fiesta | Eventos", "mujer/zapatos/fiesta-n6672", "Mujer > Colección > Zapatos > Fiesta | Eventos"),
                new CategoriaPullBear(1030527552L, Seccion.MUJER, "Zapatos", "Zapatos de tacón", "mujer/zapatos/tacon-n6927", "Mujer > Colección > Zapatos > Zapatos de tacón"),
                new CategoriaPullBear(1030207007L, Seccion.MUJER, "Zapatos", "Sandalias planas", "mujer/zapatos/sandalias-planas-n6679", "Mujer > Colección > Zapatos > Sandalias planas"),
                new CategoriaPullBear(1030243081L, Seccion.MUJER, "Zapatos", "Piel", "mujer/zapatos/piel-n6674", "Mujer > Colección > Zapatos > Piel"),

                new CategoriaPullBear(1030207022L, Seccion.MUJER, "Bolsos", "Ver todo", "mujer/bolsos-n6878", "Mujer > Colección > Bolsos > Ver todo"),
                new CategoriaPullBear(1030711206L, Seccion.MUJER, "Bolsos", "Novedades", "mujer/novedades/bolsos-n7026", "Mujer > Colección > Bolsos > Novedades"),
                new CategoriaPullBear(1030207032L, Seccion.MUJER, "Bolsos", "Fiesta | Eventos", "mujer/bolsos/fiesta-n7249", "Mujer > Colección > Bolsos > Fiesta | Eventos"),
                new CategoriaPullBear(1030207024L, Seccion.MUJER, "Bolsos", "Bolsos grandes", "mujer/bolsos/shoppers-n6889", "Mujer > Colección > Bolsos > Bolsos grandes"),
                new CategoriaPullBear(1030207025L, Seccion.MUJER, "Bolsos", "Bandoleras", "mujer/bolsos/bandoleras-n6880", "Mujer > Colección > Bolsos > Bandoleras"),
                new CategoriaPullBear(1030207027L, Seccion.MUJER, "Bolsos", "Carteras | Neceseres", "mujer/bolsos/carteras-n6453", "Mujer > Colección > Bolsos > Carteras | Neceseres"),

                new CategoriaPullBear(1030204877L, Seccion.MUJER, "Accesorios", "Ver todo", "mujer/accesorios-n6826", "Mujer > Colección > Accesorios > Ver todo"),
                new CategoriaPullBear(1030207065L, Seccion.MUJER, "Accesorios", "Gafas de sol", "mujer/accesorios/gafas-de-sol-n6456", "Mujer > Colección > Accesorios > Gafas de sol"),
                new CategoriaPullBear(1030204883L, Seccion.MUJER, "Accesorios", "Pañuelos | Bandanas", "mujer/accesorios/bufandas-y-fulares-n6452", "Mujer > Colección > Accesorios > Pañuelos | Bandanas"),
                new CategoriaPullBear(1030207068L, Seccion.MUJER, "Accesorios", "Gorros | Gorras", "mujer/accesorios/gorros-y-sombreros-n6457", "Mujer > Colección > Accesorios > Gorros | Gorras"),

                new CategoriaPullBear(1030204792L, Seccion.HOMBRE, "Camisetas", "Ver todo", "hombre/ropa/camisetas-n6323", "Hombre > Colección > Camisetas > Ver todo"),
                new CategoriaPullBear(1030204797L, Seccion.HOMBRE, "Camisetas", "Básicas", "hombre/ropa/basicos/camisetas-n6302", "Hombre > Colección > Camisetas > Básicas"),

                new CategoriaPullBear(1030204713L, Seccion.HOMBRE, "Bermudas", "Ver todo", "hombre/ropa/bermudas-n6308", "Hombre > Colección > Bermudas > Ver todo"),
                new CategoriaPullBear(1030204714L, Seccion.HOMBRE, "Bermudas", "Denim", "hombre/ropa/bermudas/denim-n6310", "Hombre > Colección > Bermudas > Denim"),

                new CategoriaPullBear(1030204731L, Seccion.HOMBRE, "Jeans", "Ver todo", "hombre/ropa/jeans-n6347", "Hombre > Colección > Jeans > Ver todo"),
                new CategoriaPullBear(1030409818L, Seccion.HOMBRE, "Jeans", "Bermudas", "hombre/ropa/bermudas/denim-n6310", "Hombre > Colección > Jeans > Bermudas"),
                new CategoriaPullBear(1030352071L, Seccion.HOMBRE, "Jeans", "Standard", "hombre/ropa/jeans/standard-fit-n7150", "Hombre > Colección > Jeans > Standard"),
                new CategoriaPullBear(1030526550L, Seccion.HOMBRE, "Jeans", "Fit Guide", "hombre/ropa/jeans-n6347", "Hombre > Colección > Jeans > Fit Guide"),

                new CategoriaPullBear(1030204721L, Seccion.HOMBRE, "Pantalones", "Ver todo", "hombre/ropa/pantalones-n6363", "Hombre > Colección > Pantalones > Ver todo"),

                new CategoriaPullBear(1030722797L, Seccion.HOMBRE, "Polos", "Ver todo", "hombre/ropa/camisetas/polos-n6371", "Hombre > Colección > Polos > Ver todo"),
                new CategoriaPullBear(1030722798L, Seccion.HOMBRE, "Polos", "Manga corta", "hombre/ropa/camisetas/polos/manga-corta-n7979", "Hombre > Colección > Polos > Manga corta"),
                new CategoriaPullBear(1030723297L, Seccion.HOMBRE, "Polos", "Manga larga", "hombre/ropa/camisetas/polos/manga-larga-n7980", "Hombre > Colección > Polos > Manga larga"),

                new CategoriaPullBear(1030204767L, Seccion.HOMBRE, "Camisas", "Ver todo", "hombre/ropa/camisas-n6313", "Hombre > Colección > Camisas > Ver todo"),

                new CategoriaPullBear(1030204823L, Seccion.HOMBRE, "Sudaderas", "Ver todo", "hombre/ropa/sudaderas-n6382", "Hombre > Colección > Sudaderas > Ver todo"),
                new CategoriaPullBear(1030204824L, Seccion.HOMBRE, "Sudaderas", "Básicas", "hombre/ropa/basicos/sudaderas-n6306", "Hombre > Colección > Sudaderas > Básicas"),

                new CategoriaPullBear(1030204710L, Seccion.HOMBRE, "Baño", "Baño", "hombre/ropa/banadores-n6299", "Hombre > Colección > Baño"),

                new CategoriaPullBear(1030204757L, Seccion.HOMBRE, "Punto", "Ver todo", "hombre/ropa/punto/jerseis-n6378", "Hombre > Colección > Punto > Ver todo"),
                new CategoriaPullBear(1030684609L, Seccion.HOMBRE, "Punto", "Polos", "hombre/ropa/punto/polos-n7915", "Hombre > Colección > Punto > Polos"),

                new CategoriaPullBear(1030204838L, Seccion.HOMBRE, "Cazadoras", "Ver todo", "hombre/ropa/cazadoras-n6335", "Hombre > Colección > Cazadoras > Ver todo"),
                new CategoriaPullBear(1030299061L, Seccion.HOMBRE, "Chándal", "Chándal", "hombre/ropa/chandal-joggers-n7337", "Hombre > Colección > Chándal"),

                new CategoriaPullBear(1030722795L, Seccion.HOMBRE, "Con lino", "Ver todo", "hombre/ropa/lino-n7414", "Hombre > Colección > Con lino > Ver todo"),

                new CategoriaPullBear(29512L, Seccion.HOMBRE, "Básicos", "Básicos", "hombre/ropa/basicos-n6300", "Hombre > Colección > Básicos"),
                new CategoriaPullBear(1030321534L, Seccion.HOMBRE, "Total look", "Total look", "hombre/ropa/twin-sets-n7569", "Hombre > Colección > Total look"),

                new CategoriaPullBear(1030207045L, Seccion.HOMBRE, "Zapatos", "Ver todo", "hombre/zapatos-n6414", "Hombre > Colección > Zapatos > Ver todo"),
                new CategoriaPullBear(739505L, Seccion.HOMBRE, "Zapatos", "Novedades", "hombre/novedades/zapatos-n6839", "Hombre > Colección > Zapatos > Novedades"),
                new CategoriaPullBear(1030243084L, Seccion.HOMBRE, "Zapatos", "Piel", "hombre/zapatos/piel-n6406", "Hombre > Colección > Zapatos > Piel"),

                new CategoriaPullBear(1030465398L, Seccion.HOMBRE, "Bolsos | Mochilas", "Ver todo", "hombre/bolsos-n7532", "Hombre > Colección > Bolsos | Mochilas > Ver todo"),
                new CategoriaPullBear(1030711207L, Seccion.HOMBRE, "Bolsos | Mochilas", "Novedades", "hombre/novedades/bolsos-n7948", "Hombre > Colección > Bolsos | Mochilas > Novedades"),
                new CategoriaPullBear(1030679107L, Seccion.HOMBRE, "Bolsos | Mochilas", "Carteras | Neceseres", "hombre/accesorios/carteras-n6233", "Hombre > Colección > Bolsos | Mochilas > Carteras | Neceseres"),

                new CategoriaPullBear(1030207098L, Seccion.HOMBRE, "Accesorios", "Ver todo", "hombre/accesorios-n6245", "Hombre > Colección > Accesorios > Ver todo"),
                new CategoriaPullBear(1030207095L, Seccion.HOMBRE, "Accesorios", "Carteras", "hombre/accesorios/carteras-n6233", "Hombre > Colección > Accesorios > Carteras")
        );
    }

    private boolean procesarCategoria(
            Page page,
            CategoriaPullBear categoriaPullBear,
            Map<String, Producto> productosGlobales,
            Tienda tienda
    ) {
        try {
            List<String> productIds = obtenerProductIdsCategoria(page, categoriaPullBear);

            if (productIds.isEmpty()) {
                return true;
            }

            List<String> productIdsPendientes = filtrarIdsNoProcesados(productIds, productosGlobales);

            if (productIdsPendientes.isEmpty()) {
                return true;
            }

            List<JsonNode> productosJson = cargarProductosCategoria(page, categoriaPullBear, productIdsPendientes);

            for (JsonNode productoJson : productosJson) {
                Producto producto = convertirJsonAProducto(productoJson, categoriaPullBear, tienda);

                if (producto == null) {
                    continue;
                }

                String claveProducto = obtenerClaveProducto(productoJson, producto);

                if (estaVacio(claveProducto)) {
                    continue;
                }

                if (productosGlobales.containsKey(claveProducto)) {
                    continue;
                }

                productosGlobales.put(claveProducto, producto);
            }

            return true;

        } catch (Exception e) {
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

            boolean yaProcesado = productosGlobales.keySet().stream()
                    .anyMatch(clave -> clave.contains("pelement=" + id) || clave.endsWith("/" + id));

            if (yaProcesado) {
                continue;
            }

            idsPendientes.add(id);
        }

        return idsPendientes;
    }

    private List<String> obtenerProductIdsCategoria(Page page, CategoriaPullBear categoriaPullBear) throws Exception {
        String endpoint = BASE_URL
                + "/itxrest/3"
                + "/catalog/store/" + STORE_ID
                + "/" + CATALOG_ID
                + "/category/" + categoriaPullBear.id()
                + "/product?languageId=" + LANGUAGE_ID
                + "&showProducts=false"
                + "&priceFilter=true"
                + "&appId=1";

        String referer = BASE_URL + "/es/" + categoriaPullBear.url();

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
                        String id = texto(component, "ccId");
                        if (!estaVacio(id)) {
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
            CategoriaPullBear categoriaPullBear,
            List<String> productIds
    ) throws Exception {
        List<JsonNode> productos = new ArrayList<>();
        List<List<String>> bloques = dividirEnBloques(productIds, TAMANO_BLOQUE_PRODUCTOS);

        for (List<String> bloque : bloques) {
            String idsTexto = String.join(",", bloque);

            String endpoint = BASE_URL
                    + "/itxrest/3"
                    + "/catalog/store/" + STORE_ID
                    + "/" + CATALOG_ID
                    + "/productsArray?languageId=" + LANGUAGE_ID
                    + "&productIds=" + idsTexto
                    + "&appId=1";

            String referer = BASE_URL + "/es/" + categoriaPullBear.url();

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
            try {
                page.navigate(referer, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(60000)
                );

                esperar(PAUSA_REINTENTO_403_MS);
                aceptarCookiesSiAparece(page);
            } catch (Exception e) {
            }

            respuesta = fetchDesdeNavegador(page, endpoint, referer);
        }

        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
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
        String body = respuestaJson.path("body").asText("");

        return new RespuestaFetch(status, body);
    }

    private Producto convertirJsonAProducto(
            JsonNode productoJson,
            CategoriaPullBear categoriaPullBear,
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

        if (normalizarTexto(nombre).contains("SHOP THE LOOK")) {
            return null;
        }

        JsonNode detail = obtenerDetailReal(productoJson);

        String descripcion = texto(detail, "longDescription");

        if (estaVacio(descripcion)) {
            descripcion = texto(detail, "description");
        }

        DatosPrecioPullBear datosPrecio = obtenerDatosPrecio(detail);

        BigDecimal precio = datosPrecio.precio();
        BigDecimal precioOriginal = datosPrecio.precioOriginal();
        Integer porcentajeDescuento = datosPrecio.porcentajeDescuento();
        boolean enOferta = datosPrecio.enOferta();

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            precio = convertirPrecio(texto(productoJson, "price"));
            precioOriginal = null;
            porcentajeDescuento = null;
            enOferta = false;
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

        Seccion seccion = convertirSeccionPullBear(sectionNameEN, categoriaPullBear.seccion());

        String nombreCategoria = normalizarCategoriaPullBear(
                nombre,
                familyName,
                subFamilyName,
                categoriaPullBear.categoria()
        );

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreCategoria);

        boolean nuevaColeccion = esCategoriaNuevaColeccion(categoriaPullBear);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setPrecioOriginal(precioOriginal);
        producto.setPorcentajeDescuento(porcentajeDescuento);
        producto.setEnOferta(enOferta);
        producto.setNuevaColeccion(nuevaColeccion);
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

    private boolean esCategoriaNuevaColeccion(CategoriaPullBear categoriaPullBear) {
        if (categoriaPullBear == null) {
            return false;
        }

        return categoriaPullBear.id() == 1030017536L
                || categoriaPullBear.id() == 1030017537L;
    }

    private JsonNode obtenerDetailReal(JsonNode productoJson) {
        JsonNode resumen = primerElemento(productoJson.path("bundleProductSummaries"));
        JsonNode detailResumen = resumen.path("detail");

        if (detailResumen.isObject() && detailResumen.size() > 0) {
            JsonNode colors = detailResumen.path("colors");

            if (colors.isArray() && colors.size() > 0) {
                return detailResumen;
            }
        }

        JsonNode detailProducto = productoJson.path("detail");

        if (detailProducto.isObject() && detailProducto.size() > 0) {
            return detailProducto;
        }

        return objectMapper.createObjectNode();
    }

    private DatosPrecioPullBear obtenerDatosPrecio(JsonNode detail) {
        JsonNode colors = detail.path("colors");

        if (!colors.isArray()) {
            return new DatosPrecioPullBear(null, null, null, false);
        }

        for (JsonNode color : colors) {
            JsonNode sizes = color.path("sizes");

            if (!sizes.isArray()) {
                continue;
            }

            for (JsonNode size : sizes) {
                BigDecimal precio = convertirPrecio(texto(size, "price"));
                BigDecimal precioOriginal = convertirPrecio(texto(size, "oldPrice"));

                Integer porcentajeDescuento = obtenerPorcentajeDescuento(size, precio, precioOriginal);

                boolean enOferta = precio != null
                        && precioOriginal != null
                        && precio.compareTo(BigDecimal.ZERO) > 0
                        && precioOriginal.compareTo(precio) > 0;

                if (precio != null && precio.compareTo(BigDecimal.ZERO) > 0) {
                    return new DatosPrecioPullBear(
                            precio,
                            precioOriginal,
                            porcentajeDescuento,
                            enOferta
                    );
                }
            }
        }

        return new DatosPrecioPullBear(null, null, null, false);
    }

    private Integer obtenerPorcentajeDescuento(JsonNode size, BigDecimal precio, BigDecimal precioOriginal) {
        String descuentoTexto = texto(size.path("discountsPercentages"), "oldPriceDiscount");

        if (!estaVacio(descuentoTexto)) {
            try {
                return Integer.parseInt(descuentoTexto.trim());
            } catch (Exception ignored) {
            }
        }

        if (precio == null || precioOriginal == null) {
            return null;
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0 || precioOriginal.compareTo(precio) <= 0) {
            return null;
        }

        try {
            BigDecimal diferencia = precioOriginal.subtract(precio);

            return diferencia
                    .multiply(BigDecimal.valueOf(100))
                    .divide(precioOriginal, 0, RoundingMode.HALF_UP)
                    .intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizarCategoriaPullBear(
            String nombreProducto,
            String familyName,
            String subFamilyName,
            String categoriaPadre
    ) {
        String padre = normalizarCategoriaPadrePullBear(categoriaPadre);

        if (!estaVacio(padre) && !esCategoriaMixtaPullBear(categoriaPadre)) {
            return padre;
        }

        return normalizarCategoria(
                nombreProducto,
                familyName,
                subFamilyName,
                categoriaPadre,
                categoriaPadre
        );
    }

    private String normalizarCategoriaPadrePullBear(String categoriaPadre) {
        String origen = normalizarTexto(categoriaPadre);

        if (origen.contains("NUEVA COLECCION") || origen.contains("NOVEDADES")) {
            return "";
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
                || origen.contains("CHAQUETAS")) {
            return "Chaquetas";
        }

        if (origen.contains("SUDADERAS")) {
            return "Sudaderas";
        }

        if (origen.contains("PUNTO")) {
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

    private boolean esCategoriaMixtaPullBear(String categoriaPadre) {
        String origen = normalizarTexto(categoriaPadre);

        return origen.contains("NUEVA COLECCION")
                || origen.contains("NOVEDADES")
                || origen.contains("BASICOS")
                || origen.contains("TOTAL LOOK")
                || origen.contains("CON LINO")
                || origen.contains("CHANDAL");
    }

    private String obtenerClaveProducto(JsonNode productoJson, Producto producto) {
        if (producto != null && !estaVacio(producto.getUrlProducto())) {
            return limpiarUrlProductoPullBear(producto.getUrlProducto());
        }

        String productUrl = texto(productoJson, "productUrl");

        if (!estaVacio(productUrl)) {
            return limpiarUrlProductoPullBear(productUrl);
        }

        String id = texto(productoJson, "id");

        if (!estaVacio(id)) {
            return id;
        }

        return "";
    }

    private String limpiarUrlProductoPullBear(String urlProducto) {
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
            return urlLimpia + "?cS=" + colorId + "&pelement=" + pelement;
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

    private Seccion convertirSeccionPullBear(String sectionNameEN, Seccion seccionFallback) {
        if (sectionNameEN == null || sectionNameEN.isBlank()) {
            return seccionFallback;
        }

        String seccionNormalizada = normalizarTexto(sectionNameEN);

        if (seccionNormalizada.contains("WOMEN") || seccionNormalizada.contains("MUJER")) {
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

        if (empiezaPorAlgunaPalabra(nombre, "CAMISETA", "TOP", "BODY")) {
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

        if (empiezaPorAlgunaPalabra(nombre, "BERMUDA", "BERMUDAS", "SHORT", "SHORTS", "JORTS")) {
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

        if (contieneAlgunaPalabra(familia, "BERMUDA", "BERMUDAS", "SHORT", "SHORTS")) {
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

        if (origen.contains("CAZADORAS") || origen.contains("GABARDINAS") || origen.contains("BLAZERS")) {
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
                "ZAPATO", "ZAPATOS",
                "ZAPATILLA", "ZAPATILLAS",
                "BOTA", "BOTAS",
                "BOTIN", "BOTÍN", "BOTINES",
                "SANDALIA", "SANDALIAS",
                "MOCASIN", "MOCASÍN", "MOCASINES",
                "ALPARGATA", "ALPARGATAS",
                "BAILARINA", "BAILARINAS",
                "MULE", "MULES",
                "TACON", "TACÓN", "TACONES",
                "RUNNING",
                "CALZADO"
        );
    }

    private boolean esBolso(String texto) {
        return contieneAlgunaPalabra(texto,
                "BOLSO", "BOLSOS",
                "MOCHILA", "MOCHILAS",
                "CARTERA", "CARTERAS",
                "MONEDERO", "MONEDEROS",
                "RIÑONERA", "RIÑONERAS",
                "RINONERA", "RINONERAS",
                "NECESER", "NECESERES",
                "SHOPPER", "SHOPPERS",
                "BANDOLERA", "BANDOLERAS"
        );
    }

    private boolean esAccesorio(String texto) {
        return contieneAlgunaPalabra(texto,
                "ACCESORIO", "ACCESORIOS",
                "CINTURON", "CINTURÓN", "CINTURONES",
                "GORRA", "GORRAS",
                "GORRO", "GORROS",
                "SOMBRERO", "SOMBREROS",
                "PAÑUELO", "PAÑUELOS",
                "PANUELO", "PANUELOS",
                "BUFANDA", "BUFANDAS",
                "GAFAS",
                "LLAVERO", "LLAVEROS",
                "COLLAR", "COLLARES",
                "PENDIENTE", "PENDIENTES",
                "PULSERA", "PULSERAS",
                "ANILLO", "ANILLOS",
                "CALCETIN", "CALCETÍN", "CALCETINES",
                "BISUTERIA", "BISUTERÍA",
                "PERFUME", "PERFUMES",
                "FRAGANCIA", "FRAGANCIAS"
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
        if (precioTexto == null || precioTexto.isBlank() || "null".equalsIgnoreCase(precioTexto.trim())) {
            return null;
        }

        try {
            BigDecimal precioCentimos = new BigDecimal(precioTexto.trim());
            return precioCentimos.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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
    }

    private String userAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/147.0.0.0 Safari/537.36";
    }

    private void imprimirResumenFinal(List<Producto> productos, int categoriasOk, int categoriasFallidas) {
    }

    private record CategoriaPullBear(
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

    private record DatosPrecioPullBear(
            BigDecimal precio,
            BigDecimal precioOriginal,
            Integer porcentajeDescuento,
            boolean enOferta
    ) {
    }
}