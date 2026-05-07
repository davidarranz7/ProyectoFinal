package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.ProductoImagen;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.model.Tienda;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZaraScraper implements ScraperTienda {

    private static final String BASE_URL = "https://www.zara.com/es/es";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public String getNombreTienda() {
        return "Zara";
    }

    @Override
    public List<Producto> scrapearProductos() {
        List<CategoriaZara> categoriasZara = obtenerCategoriasZara();

        Map<String, Producto> productosGlobales = new LinkedHashMap<>();
        Set<String> urlsVistas = new HashSet<>();

        Tienda tienda = new Tienda();
        tienda.setNombre("Zara");
        tienda.setUrl("https://www.zara.com");

        System.out.println("====================================");
        System.out.println("SCRAPING ZARA - API JSON");
        System.out.println("====================================");
        System.out.println("Categorías a procesar: " + categoriasZara.size());
        System.out.println();

        for (CategoriaZara categoriaZara : categoriasZara) {
            procesarCategoria(categoriaZara, productosGlobales, urlsVistas, tienda);
        }

        List<Producto> productosFinales = new ArrayList<>(productosGlobales.values());

        imprimirResumenFinal(productosFinales);

        return productosFinales;
    }

    private List<CategoriaZara> obtenerCategoriasZara() {
        return List.of(
                new CategoriaZara("Mujer vestidos", "2420896"),
                new CategoriaZara("Mujer chaquetas", "2417772"),
                new CategoriaZara("Mujer abrigos", "2419032"),
                new CategoriaZara("Mujer camisas", "2420369"),
                new CategoriaZara("Mujer camisetas", "2420417"),
                new CategoriaZara("Mujer jeans", "2419185"),
                new CategoriaZara("Mujer pantalones", "2420795"),
                new CategoriaZara("Mujer faldas", "2420454"),
                new CategoriaZara("Mujer jerséis", "2419845"),
                new CategoriaZara("Mujer sudaderas", "2467841"),
                new CategoriaZara("Mujer trajes", "2419759"),
                new CategoriaZara("Mujer zapatos", "2419160"),
                new CategoriaZara("Mujer bolsos", "2417728"),
                new CategoriaZara("Mujer accesorios", "2417727"),

                new CategoriaZara("Mujer nueva colección", "2546081", true),

                new CategoriaZara("Mujer precios especiales", "2419737"),
                new CategoriaZara("Hombre precios especiales", "2436823"),

                new CategoriaZara("Hombre lino", "2431961"),
                new CategoriaZara("Hombre camisas", "2431994"),
                new CategoriaZara("Hombre camisetas", "2432041"),
                new CategoriaZara("Hombre pantalones", "2432096"),
                new CategoriaZara("Hombre jeans", "2432131"),
                new CategoriaZara("Hombre trajes", "2432192"),
                new CategoriaZara("Hombre sudaderas", "2432232"),
                new CategoriaZara("Hombre punto", "2432265"),
                new CategoriaZara("Hombre abrigos", "2606109"),
                new CategoriaZara("Hombre zapatos", "2436382"),
                new CategoriaZara("Hombre bolsos", "2436405"),
                new CategoriaZara("Hombre accesorios", "2436431")
        );
    }

    private void procesarCategoria(
            CategoriaZara categoriaZara,
            Map<String, Producto> productosGlobales,
            Set<String> urlsVistas,
            Tienda tienda
    ) {
        String endpoint = BASE_URL + "/category/" + categoriaZara.id() + "/products?ajax=true";

        int productosBrutos = 0;
        int productosNuevos = 0;
        int productosRepetidos = 0;
        int productosDescartados = 0;
        int urlsFallback = 0;
        int productosEnOfertaCategoria = 0;
        int productosNuevaColeccionCategoria = 0;

        System.out.println("====================================");
        System.out.println("PROCESANDO CATEGORÍA ZARA");
        System.out.println("====================================");
        System.out.println("Nombre: " + categoriaZara.nombre());
        System.out.println("ID: " + categoriaZara.id());
        System.out.println("Nueva colección: " + categoriaZara.nuevaColeccion());
        System.out.println("Endpoint: " + endpoint);

        try {
            JsonNode root = hacerPeticion(endpoint);
            JsonNode productGroups = root.path("productGroups");

            if (!productGroups.isArray()) {
                System.out.println("No existe productGroups o no es array.");
                System.out.println();
                return;
            }

            for (JsonNode grupo : productGroups) {
                JsonNode elements = grupo.path("elements");

                if (!elements.isArray()) {
                    continue;
                }

                for (JsonNode element : elements) {
                    JsonNode commercialComponents = element.path("commercialComponents");

                    if (!commercialComponents.isArray()) {
                        continue;
                    }

                    for (JsonNode componente : commercialComponents) {
                        String type = texto(componente, "type");

                        if (!"Product".equals(type)) {
                            continue;
                        }

                        productosBrutos++;

                        Producto producto = convertirJsonAProducto(componente, categoriaZara, tienda);

                        if (producto == null) {
                            productosDescartados++;
                            continue;
                        }

                        if (Boolean.TRUE.equals(producto.getEnOferta())) {
                            productosEnOfertaCategoria++;
                        }

                        if (Boolean.TRUE.equals(producto.getNuevaColeccion())) {
                            productosNuevaColeccionCategoria++;
                        }

                        if (producto.getUrlProducto() != null && producto.getUrlProducto().contains("slug-fallback-debug")) {
                            urlsFallback++;
                        }

                        String claveProducto = obtenerClaveProducto(componente, producto);

                        if (estaVacio(claveProducto)) {
                            productosDescartados++;
                            continue;
                        }

                        if (productosGlobales.containsKey(claveProducto)) {
                            Producto productoExistente = productosGlobales.get(claveProducto);

                            if (categoriaZara.nuevaColeccion()) {
                                productoExistente.setNuevaColeccion(true);
                            }

                            productosRepetidos++;
                            continue;
                        }

                        if (urlsVistas.contains(producto.getUrlProducto())) {
                            if (categoriaZara.nuevaColeccion()) {
                                productosGlobales.values().stream()
                                        .filter(p -> producto.getUrlProducto().equals(p.getUrlProducto()))
                                        .findFirst()
                                        .ifPresent(p -> p.setNuevaColeccion(true));
                            }

                            productosRepetidos++;
                            continue;
                        }

                        productosGlobales.put(claveProducto, producto);
                        urlsVistas.add(producto.getUrlProducto());
                        productosNuevos++;
                    }
                }
            }

            System.out.println("OK categoría procesada.");
            System.out.println("Productos brutos encontrados: " + productosBrutos);
            System.out.println("Productos nuevos añadidos: " + productosNuevos);
            System.out.println("Productos repetidos ignorados: " + productosRepetidos);
            System.out.println("Productos descartados: " + productosDescartados);
            System.out.println("Productos en oferta detectados en categoría: " + productosEnOfertaCategoria);
            System.out.println("Productos nueva colección detectados en categoría: " + productosNuevaColeccionCategoria);
            System.out.println("URLs fallback generadas: " + urlsFallback);
            System.out.println();

        } catch (Exception e) {
            System.out.println("ERROR procesando categoría: " + categoriaZara.nombre());
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println();
        }
    }

    private JsonNode hacerPeticion(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .GET()
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://www.zara.com/es/")
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("HTTP status: " + response.statusCode());
        System.out.println("Longitud respuesta: " + response.body().length());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Respuesta HTTP no válida: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private Producto convertirJsonAProducto(JsonNode productoJson, CategoriaZara categoriaZara, Tienda tienda) {
        String seoProductId = texto(productoJson.path("seo"), "seoProductId");
        String productId = texto(productoJson, "id");
        String nombre = texto(productoJson, "name");
        String descripcion = texto(productoJson, "description");
        String keyword = texto(productoJson.path("seo"), "keyword");

        if (estaVacio(descripcion)) {
            System.out.println("====================================");
            System.out.println("DEBUG DESCRIPCION ZARA");
            System.out.println("Producto: " + nombre);
            System.out.println("Categoria: " + categoriaZara.nombre());
            System.out.println("Campos raíz del producto:");

            productoJson.fieldNames().forEachRemaining(campo -> {
                System.out.println("- " + campo);
            });

            System.out.println("Campos dentro de detail:");

            JsonNode detail = productoJson.path("detail");

            if (!detail.isMissingNode() && !detail.isNull()) {
                detail.fieldNames().forEachRemaining(campo -> {
                    System.out.println("- detail." + campo);
                });
            }

            System.out.println("description raíz: " + texto(productoJson, "description"));
            System.out.println("detail.description: " + texto(detail, "description"));
            System.out.println("detail.longDescription: " + texto(detail, "longDescription"));
            System.out.println("detail.shortDescription: " + texto(detail, "shortDescription"));
            System.out.println("====================================");
        }

        if (estaVacio(seoProductId) || estaVacio(productId) || estaVacio(nombre)) {
            return null;
        }

        BigDecimal precio = convertirPrecio(productoJson.path("price").asText("0"));
        BigDecimal precioOriginal = convertirPrecio(productoJson.path("oldPrice").asText(""));

        Integer porcentajeDescuento = productoJson.hasNonNull("displayDiscountPercentage")
                ? productoJson.path("displayDiscountPercentage").asInt()
                : null;

        boolean enOferta = precioOriginal != null
                && precio != null
                && precioOriginal.compareTo(precio) > 0;

        if (porcentajeDescuento != null && porcentajeDescuento > 0) {
            enOferta = true;
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String familyName = texto(productoJson, "familyName");
        String subfamilyName = texto(productoJson, "subfamilyName");
        String sectionName = texto(productoJson, "sectionName");

        Seccion seccion = convertirSeccionZara(sectionName);

        String nombreCategoria = normalizarCategoria(
                nombre,
                familyName,
                subfamilyName,
                categoriaZara.nombre(),
                keyword
        );

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreCategoria);

        String urlProducto = crearUrlProducto(keyword, nombre, seoProductId, productId);

        if (estaVacio(urlProducto)) {
            return null;
        }

        List<String> imagenesExtraidas = extraerImagenesProducto(productoJson);

        String imagenPrincipal = imagenesExtraidas.isEmpty()
                ? ""
                : imagenesExtraidas.get(0);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setPrecioOriginal(precioOriginal);
        producto.setPorcentajeDescuento(porcentajeDescuento);
        producto.setEnOferta(enOferta);
        producto.setNuevaColeccion(categoriaZara.nuevaColeccion());
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

    private String obtenerClaveProducto(JsonNode productoJson, Producto producto) {
        String seoProductId = texto(productoJson.path("seo"), "seoProductId");

        if (!estaVacio(seoProductId)) {
            return seoProductId;
        }

        if (producto != null && !estaVacio(producto.getUrlProducto())) {
            return producto.getUrlProducto();
        }

        return "";
    }

    private Seccion convertirSeccionZara(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return Seccion.UNISEX;
        }

        String seccionNormalizada = sectionName.trim().toUpperCase();

        return switch (seccionNormalizada) {
            case "MAN" -> Seccion.HOMBRE;
            case "WOMAN" -> Seccion.MUJER;
            case "KID" -> Seccion.UNISEX;
            default -> Seccion.UNISEX;
        };
    }

    private String normalizarCategoria(
            String nombreProducto,
            String familyName,
            String subfamilyName,
            String categoriaOrigen,
            String keyword
    ) {
        String nombre = normalizarTexto(nombreProducto);
        String familia = normalizarTexto(familyName);
        String subfamilia = normalizarTexto(subfamilyName);
        String origen = normalizarTexto(categoriaOrigen);
        String keywordNormalizado = normalizarTexto(keyword);

        String textoDenim = unirTextos(nombre, familia, subfamilia, origen, keywordNormalizado);

        if (empiezaPorAlgunaPalabra(nombre, "VESTIDO")) {
            return "Vestidos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "MONO")) {
            return "Vestidos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "FALDA")) {
            return "Faldas";
        }

        if (esZapatoPorTexto(nombre) || esZapatoPorJson(familia, subfamilia)) {
            return "Zapatos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "BOLSO", "MOCHILA", "CARTERA", "MONEDERO", "MALETA", "MALETIN", "SHOPPER", "CLUTCH")) {
            return "Bolsos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CINTURON", "GORRA", "SOMBRERO", "PANUELO", "BUFANDA", "CORBATA", "GAFAS", "LLAVERO", "COLLAR", "PENDIENTE")) {
            return "Accesorios";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CAMISA", "SOBRECAMISA", "BLUSA")) {
            return "Camisas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CAMISETA", "CAMISET", "TOP", "POLO")) {
            return "Camisetas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "CHAQUETA", "CAZADORA", "BOMBER")) {
            return "Chaquetas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "BLAZER")) {
            if (contieneAlgunaPalabra(nombre, "TRAJE")) {
                return "Trajes";
            }

            return "Chaquetas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "ABRIGO", "GABARDINA", "TRENCH", "PARKA", "ANORAK")) {
            return "Abrigos";
        }

        if (empiezaPorAlgunaPalabra(nombre, "SUDADERA")) {
            return "Sudaderas";
        }

        if (empiezaPorAlgunaPalabra(nombre, "JERSEY", "CARDIGAN")) {
            return "Jerséis";
        }

        if (empiezaPorAlgunaPalabra(nombre, "PANTALON", "BERMUDA", "SHORT", "SHORTS", "LEGGING", "LEGGINGS", "JOGGER")) {
            if (esDenim(textoDenim)) {
                return "Jeans";
            }

            return "Pantalones";
        }

        if (esBolsoPorJson(familia)) {
            return "Bolsos";
        }

        if (esAccesorioPorJson(familia)) {
            return "Accesorios";
        }

        if (esVestidoPorJson(familia)) {
            return "Vestidos";
        }

        if (esFaldaPorJson(familia)) {
            return "Faldas";
        }

        if (esCamisaPorJson(familia)) {
            return "Camisas";
        }

        if (esCamisetaPorJson(familia)) {
            return "Camisetas";
        }

        if (esChaquetaPorJson(familia)) {
            return "Chaquetas";
        }

        if (esAbrigoPorJson(familia)) {
            return "Abrigos";
        }

        if (esSudaderaPorJson(familia)) {
            return "Sudaderas";
        }

        if (esPantalonPorJson(familia)) {
            if (esDenim(textoDenim)) {
                return "Jeans";
            }

            return "Pantalones";
        }

        if (esJerseyPorJson(familia, subfamilia)) {
            return "Jerséis";
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

        if (origen.contains("CAMISAS")) {
            return "Camisas";
        }

        if (origen.contains("CAMISETAS")) {
            return "Camisetas";
        }

        if (origen.contains("CHAQUETAS")) {
            return "Chaquetas";
        }

        if (origen.contains("ABRIGOS")) {
            return "Abrigos";
        }

        if (origen.contains("SUDADERAS")) {
            return "Sudaderas";
        }

        if (origen.contains("ZAPATOS")) {
            return "Zapatos";
        }

        if (origen.contains("BOLSOS")) {
            return "Bolsos";
        }

        if (origen.contains("ACCESORIOS")) {
            return "Accesorios";
        }

        return "Otros";
    }

    private boolean esZapatoPorJson(String familia, String subfamilia) {
        return contieneAlgunaPalabra(familia,
                "ZAPATO", "ZAPATO PLANO", "ZAPATO TACON",
                "SANDALIA", "SANDALIA DEPORTIVA", "SANDALIA E",
                "BOTA", "BOTA PLANA", "BOTA TACON",
                "BOTIN", "BOTIN PLANO", "BOTIN TACON",
                "MOCASIN", "RUNNING", "BAMBAS", "DEPORTIVO",
                "CALZADO DEPORTIVO", "DEPORTIVO BOTIN",
                "CUÑA", "PALA", "PINKY", "ABIERTO"
        ) || contieneAlgunaPalabra(subfamilia,
                "ZAPATO", "BOTA", "BOTIN", "DEPORTIVO",
                "MOCASIN", "RUNNING", "ABIERTO", "YUTE"
        );
    }

    private boolean esZapatoPorTexto(String nombre) {
        return empiezaPorAlgunaPalabra(nombre,
                "ZAPATO", "ZAPATILLA", "BOTA", "BOTIN",
                "SANDALIA", "MOCASIN", "ALPARGATA",
                "BAILARINA", "MULE", "TACON", "RUNNING"
        );
    }

    private boolean esBolsoPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "BOLSOS", "MONEDERO BILLETERA"
        );
    }

    private boolean esAccesorioPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "ACCESORIOS", "COMPLEMENTOS"
        );
    }

    private boolean esVestidoPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "VESTIDO", "MONO"
        );
    }

    private boolean esFaldaPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "FALDA"
        );
    }

    private boolean esCamisaPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "CAMISA", "SOBRECAMISA", "BLUSA"
        );
    }

    private boolean esCamisetaPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "CAMISETA", "POLO"
        );
    }

    private boolean esChaquetaPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "CHAQUETA", "CAZADORA", "BLAZER", "BOMBER", "CHALECO"
        );
    }

    private boolean esAbrigoPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "ABRIGO", "GABARDINA", "ANORAK", "PARKA", "TRENCH"
        );
    }

    private boolean esSudaderaPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "SUDADERA"
        );
    }

    private boolean esPantalonPorJson(String familia) {
        return contieneAlgunaPalabra(familia,
                "PANTALON", "BERMUDA", "SHORT", "LEGGINGS", "LEGGING"
        );
    }

    private boolean esJerseyPorJson(String familia, String subfamilia) {
        return contieneAlgunaPalabra(familia,
                "JERSEY", "CHALECO PUNTO"
        ) || contieneAlgunaPalabra(subfamilia,
                "JERSEY", "CHALECO PUNTO"
        );
    }

    private boolean esDenim(String texto) {
        return contieneAlgunaPalabra(texto, "DENIM", "JEANS");
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

        String textoConEspacios = " " + texto + " ";

        for (String palabra : palabras) {
            String palabraNormalizada = normalizarTexto(palabra);

            if (textoConEspacios.contains(" " + palabraNormalizada + " ")) {
                return true;
            }
        }

        return false;
    }

    private String crearUrlProducto(
            String keyword,
            String nombreProducto,
            String seoProductId,
            String productId
    ) {
        if (estaVacio(seoProductId) || estaVacio(productId)) {
            return "";
        }

        String slug = keyword;

        if (estaVacio(slug)) {
            slug = crearSlugFallback(nombreProducto);
        }

        if (estaVacio(slug)) {
            return "";
        }

        return BASE_URL + "/" + slug + "-p" + seoProductId + ".html?v1=" + productId;
    }

    private String crearSlugFallback(String nombreProducto) {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            return "";
        }

        return Normalizer.normalize(nombreProducto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replace("®", "")
                .replace("&", " ")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

    private List<String> extraerImagenesProducto(JsonNode productoJson) {
        List<String> imagenes = new ArrayList<>();
        Set<String> imagenesVistas = new HashSet<>();

        JsonNode colors = productoJson.path("detail").path("colors");

        if (!colors.isArray()) {
            return imagenes;
        }

        for (JsonNode color : colors) {
            JsonNode xmedia = color.path("xmedia");

            if (!xmedia.isArray()) {
                continue;
            }

            for (JsonNode media : xmedia) {
                String type = texto(media, "type");
                String deliveryUrl = texto(media.path("extraInfo"), "deliveryUrl");

                if (!"image".equals(type)) {
                    continue;
                }

                if (estaVacio(deliveryUrl)) {
                    continue;
                }

                if (imagenesVistas.contains(deliveryUrl)) {
                    continue;
                }

                imagenesVistas.add(deliveryUrl);
                imagenes.add(deliveryUrl);
            }
        }

        return imagenes;
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

    private String unirTextos(String... textos) {
        StringBuilder resultado = new StringBuilder();

        for (String texto : textos) {
            if (texto != null && !texto.isBlank()) {
                resultado.append(texto).append(" ");
            }
        }

        return resultado.toString().trim();
    }

    private boolean contieneAlguno(String texto, String... palabras) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }

        return false;
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private void imprimirResumenFinal(List<Producto> productos) {
        long productosSinImagen = productos.stream()
                .filter(producto -> estaVacio(producto.getUrlImagen()))
                .count();

        long productosSinPrecio = productos.stream()
                .filter(producto -> producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0)
                .count();

        long productosSinUrl = productos.stream()
                .filter(producto -> estaVacio(producto.getUrlProducto()))
                .count();

        long productosEnOferta = productos.stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getEnOferta()))
                .count();

        long productosNuevaColeccion = productos.stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getNuevaColeccion()))
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
        System.out.println("RESUMEN FINAL ZARA");
        System.out.println("====================================");
        System.out.println("Productos únicos finales: " + productos.size());
        System.out.println("Productos sin imagen principal: " + productosSinImagen);
        System.out.println("Productos sin precio: " + productosSinPrecio);
        System.out.println("Productos sin URL: " + productosSinUrl);
        System.out.println("Productos en oferta: " + productosEnOferta);
        System.out.println("Productos nueva colección: " + productosNuevaColeccion);
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
                    System.out.println("Precio original: " + producto.getPrecioOriginal());
                    System.out.println("Porcentaje descuento: " + producto.getPorcentajeDescuento());
                    System.out.println("En oferta: " + producto.getEnOferta());
                    System.out.println("Nueva colección: " + producto.getNuevaColeccion());
                    System.out.println("Sección: " + producto.getSeccion());
                    System.out.println("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria().getNombre() : ""));
                    System.out.println("URL: " + producto.getUrlProducto());
                    System.out.println("Imagen principal: " + producto.getUrlImagen());
                    System.out.println("Total imágenes: " + totalImagenesProducto);
                });

        System.out.println();
        System.out.println("SCRAPING ZARA TERMINADO.");
    }

    private record CategoriaZara(
            String nombre,
            String id,
            boolean nuevaColeccion
    ) {

        public CategoriaZara(String nombre, String id) {
            this(nombre, id, false);
        }
    }
}