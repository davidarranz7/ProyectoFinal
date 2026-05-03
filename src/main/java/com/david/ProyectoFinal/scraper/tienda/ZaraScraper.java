package com.david.ProyectoFinal.scraper.tienda;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
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

                new CategoriaZara("Hombre lino", "2431961"),
                new CategoriaZara("Hombre camisas", "2431994"),
                new CategoriaZara("Hombre jeans", "2432131"),
                new CategoriaZara("Hombre trajes", "2432192"),
                new CategoriaZara("Hombre sudaderas", "2432232"),
                new CategoriaZara("Hombre punto", "2432265"),
                new CategoriaZara("Hombre abrigos", "2606109"),
                new CategoriaZara("Hombre zapatos", "2436382")
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

        System.out.println("====================================");
        System.out.println("PROCESANDO CATEGORÍA ZARA");
        System.out.println("====================================");
        System.out.println("Nombre: " + categoriaZara.nombre());
        System.out.println("ID: " + categoriaZara.id());
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

                        if (producto.getUrlProducto() != null && producto.getUrlProducto().contains("slug-fallback-debug")) {
                            urlsFallback++;
                        }

                        String claveProducto = obtenerClaveProducto(componente, producto);

                        if (estaVacio(claveProducto)) {
                            productosDescartados++;
                            continue;
                        }

                        if (productosGlobales.containsKey(claveProducto) || urlsVistas.contains(producto.getUrlProducto())) {
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

        if (estaVacio(seoProductId) || estaVacio(productId) || estaVacio(nombre)) {
            return null;
        }

        BigDecimal precio = convertirPrecio(productoJson.path("price").asText("0"));

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
                categoriaZara.nombre()
        );

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreCategoria);

        String urlProducto = crearUrlProducto(keyword, nombre, seoProductId, productId);
        String imagen = extraerImagenPrincipal(productoJson);

        if (estaVacio(urlProducto)) {
            return null;
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setUrlImagen(imagen);
        producto.setUrlProducto(urlProducto);
        producto.setSeccion(seccion);
        producto.setCategoria(categoria);
        producto.setTienda(tienda);

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
            default -> Seccion.UNISEX;
        };
    }

    private String normalizarCategoria(
            String nombreProducto,
            String familyName,
            String subfamilyName,
            String categoriaOrigen
    ) {
        String texto = unirTextos(nombreProducto, familyName, subfamilyName, categoriaOrigen)
                .toUpperCase();

        if (contieneAlguno(texto, "VESTIDO", "MONO")) {
            return "Vestidos";
        }

        if (contieneAlguno(texto, "CAMISA", "BLUSA")) {
            return "Camisas";
        }

        if (contieneAlguno(texto, "CAMISETA", "TOP", "POLO")) {
            return "Camisetas";
        }

        if (contieneAlguno(texto, "JEANS", "DENIM")) {
            return "Jeans";
        }

        if (contieneAlguno(texto, "PANTALON", "PANTALÓN")) {
            return "Pantalones";
        }

        if (contieneAlguno(texto, "FALDA")) {
            return "Faldas";
        }

        if (contieneAlguno(texto, "JERSEY", "JERSÉI", "JERSEIS", "JERSÉIS", "PUNTO", "CARDIGAN", "CÁRDIGAN")) {
            return "Jerséis";
        }

        if (contieneAlguno(texto, "SUDADERA")) {
            return "Sudaderas";
        }

        if (contieneAlguno(texto, "TRAJE")) {
            return "Trajes";
        }

        if (contieneAlguno(texto, "ABRIGO", "GABARDINA", "ANORAK", "PARKA", "TRENCH")) {
            return "Abrigos";
        }

        if (contieneAlguno(texto, "CHAQUETA", "CAZADORA", "BLAZER", "BOMBER")) {
            return "Chaquetas";
        }

        if (contieneAlguno(texto, "ZAPATO", "ZAPATILLA", "SANDALIA", "MOCASIN", "MOCASÍN", "RUNNING", "DEPORTIVO", "BOTA", "MULE")) {
            return "Zapatos";
        }

        if (contieneAlguno(texto, "BOLSO", "BOLSOS", "SHOPPER", "CLUTCH", "MONEDERO", "CARTERA", "MOCHILA")) {
            return "Bolsos";
        }

        if (contieneAlguno(texto, "ACCESORIO", "ACCESORIOS", "LLAVERO", "CINTURON", "CINTURÓN", "GORRA", "SOMBRERO", "PAÑUELO", "BUFANDA")) {
            return "Accesorios";
        }

        return "Otros";
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

    private String extraerImagenPrincipal(JsonNode productoJson) {
        JsonNode colorDetalle = productoJson.path("detail").path("colors").isArray()
                && productoJson.path("detail").path("colors").size() > 0
                ? productoJson.path("detail").path("colors").get(0)
                : objectMapper.createObjectNode();

        JsonNode xmedia = colorDetalle.path("xmedia");

        if (!xmedia.isArray()) {
            return "";
        }

        for (JsonNode media : xmedia) {
            String type = texto(media, "type");
            String deliveryUrl = texto(media.path("extraInfo"), "deliveryUrl");

            if ("image".equals(type) && !estaVacio(deliveryUrl)) {
                return deliveryUrl;
            }
        }

        return "";
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
        System.out.println("Productos sin imagen: " + productosSinImagen);
        System.out.println("Productos sin precio: " + productosSinPrecio);
        System.out.println("Productos sin URL: " + productosSinUrl);

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
                    System.out.println("------------------------------------");
                    System.out.println("Nombre: " + producto.getNombre());
                    System.out.println("Precio: " + producto.getPrecio());
                    System.out.println("Sección: " + producto.getSeccion());
                    System.out.println("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria().getNombre() : ""));
                    System.out.println("URL: " + producto.getUrlProducto());
                    System.out.println("Imagen: " + producto.getUrlImagen());
                });

        System.out.println();
        System.out.println("SCRAPING ZARA TERMINADO.");
    }

    private record CategoriaZara(
            String nombre,
            String id
    ) {
    }
}