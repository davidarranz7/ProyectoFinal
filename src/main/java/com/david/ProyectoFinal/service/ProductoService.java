package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.*;
import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import com.david.ProyectoFinal.scraper.gestor.GestorScraping;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final GestorScraping gestorScraping;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoTallaStockRepository productoTallaStockRepository;
    private final FavoritoRepository favoritoRepository;
    private final ProductoImagenRepository productoImagenRepository;

    public ProductoService(ProductoRepository productoRepository, GestorScraping gestorScraping, TiendaRepository tiendaRepository, CategoriaRepository categoriaRepository, ProductoTallaStockRepository productoTallaStockRepository, FavoritoRepository favoritoRepository, ProductoImagenRepository productoImagenRepository) {
        this.productoRepository = productoRepository;
        this.gestorScraping = gestorScraping;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoTallaStockRepository = productoTallaStockRepository;
        this.favoritoRepository = favoritoRepository;
        this.productoImagenRepository = productoImagenRepository;
    }

    public List<Producto> obtenerProductosMasFavoritos(int limite) {
        return favoritoRepository.findProductosMasFavoritos(PageRequest.of(0, limite));
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto != null) {
            producto.setNombre(productoActualizado.getNombre());
            producto.setDescripcion(productoActualizado.getDescripcion());
            producto.setPrecio(productoActualizado.getPrecio());
            producto.setUrlImagen(productoActualizado.getUrlImagen());
            producto.setUrlProducto(productoActualizado.getUrlProducto());
            producto.setSeccion(productoActualizado.getSeccion());
            producto.setCategoria(productoActualizado.getCategoria());
            producto.setTienda(productoActualizado.getTienda());

            return productoRepository.save(producto);
        }

        return null;
    }

    public List<Producto> scrapearYGuardar() {
        List<Producto> productosScrapeados = gestorScraping.scrapearTodo();
        return guardarProductosScrapeados(productosScrapeados);
    }

    public List<Producto> scrapearZaraYGuardar() {
        List<Producto> productosScrapeados = gestorScraping.scrapearZara();
        return guardarProductosScrapeados(productosScrapeados);
    }

    public List<Producto> scrapearBershkaYGuardar() {
        List<Producto> productosScrapeados = gestorScraping.scrapearBershka();
        return guardarProductosScrapeados(productosScrapeados);
    }

    public List<Producto> scrapearPullAndBearYGuardar() {
        List<Producto> productosScrapeados = gestorScraping.scrapearPullAndBear();
        return guardarProductosScrapeados(productosScrapeados);
    }

    private List<Producto> guardarProductosScrapeados(List<Producto> productosScrapeados) {
        List<Producto> productosGuardados = new ArrayList<>();

        for (Producto producto : productosScrapeados) {

            Tienda tiendaScrapeada = producto.getTienda();

            if (tiendaScrapeada != null) {
                Optional<Tienda> tiendaExistente = tiendaRepository.findByNombre(tiendaScrapeada.getNombre());

                if (tiendaExistente.isPresent()) {
                    producto.setTienda(tiendaExistente.get());
                } else {
                    producto.setTienda(tiendaRepository.save(tiendaScrapeada));
                }
            }

            Categoria categoriaScrapeada = producto.getCategoria();

            if (categoriaScrapeada != null) {
                Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(categoriaScrapeada.getNombre());

                if (categoriaExistente.isPresent()) {
                    producto.setCategoria(categoriaExistente.get());
                } else {
                    producto.setCategoria(categoriaRepository.save(categoriaScrapeada));
                }
            }

            Optional<Producto> existente = productoRepository.findByUrlProducto(producto.getUrlProducto());

            if (existente.isPresent()) {
                Producto productoExistente = existente.get();

                productoExistente.setNombre(producto.getNombre());
                productoExistente.setDescripcion(producto.getDescripcion());
                productoExistente.setPrecio(producto.getPrecio());
                productoExistente.setUrlImagen(producto.getUrlImagen());
                productoExistente.setUrlProducto(producto.getUrlProducto());
                productoExistente.setSeccion(producto.getSeccion());
                productoExistente.setCategoria(producto.getCategoria());
                productoExistente.setTienda(producto.getTienda());

                productosGuardados.add(productoRepository.save(productoExistente));
            } else {
                productosGuardados.add(productoRepository.save(producto));
            }
        }

        return productosGuardados;
    }

    public List<Producto> obtenerPorTienda(String nombreTienda) {
        List<Producto> productos = productoRepository.findByTiendaNombre(nombreTienda);

        for (Producto producto : productos) {
            List<ProductoTallaStock> tallaStocksExistentes =
                    productoTallaStockRepository.findByProductoId(producto.getId());

            List<ProductoTallaStock> tallaStocksCompletos = Arrays.stream(Talla.values())
                    .map(tallaEnum -> {
                        ProductoTallaStock tallaEncontrada = tallaStocksExistentes.stream()
                                .filter(item -> item.getTalla() == tallaEnum)
                                .findFirst()
                                .orElse(null);

                        if (tallaEncontrada != null) {
                            return tallaEncontrada;
                        }

                        ProductoTallaStock nueva = new ProductoTallaStock();
                        nueva.setProducto(producto);
                        nueva.setTalla(tallaEnum);
                        nueva.setStock(0);
                        return nueva;
                    })
                    .toList();

            producto.setTallaStocks(tallaStocksCompletos);
        }

        return productos;
    }

    public ProductoPageResponseDTO buscarProductos(String tienda,
                                                   List<Seccion> secciones,
                                                   List<String> categorias,
                                                   String busqueda,
                                                   String orden,
                                                   int page,
                                                   int size) {
        int pagina = Math.max(page, 0);
        int tamano = Math.min(Math.max(size, 1), 48);

        Pageable pageable = PageRequest.of(pagina, tamano, obtenerOrdenProductos(orden));

        Specification<Producto> specification = (root, query, criteriaBuilder) -> {
            query.distinct(true);

            Join<Producto, Tienda> tiendaJoin = root.join("tienda", JoinType.LEFT);
            Join<Producto, Categoria> categoriaJoin = root.join("categoria", JoinType.LEFT);

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (tienda != null && !tienda.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(tiendaJoin.get("nombre")),
                                tienda.trim().toLowerCase()
                        )
                );
            }

            if (secciones != null && !secciones.isEmpty()) {
                List<Seccion> seccionesValidas = secciones.stream()
                        .filter(Objects::nonNull)
                        .toList();

                if (!seccionesValidas.isEmpty()) {
                    predicates.add(root.get("seccion").in(seccionesValidas));
                }
            }

            if (categorias != null && !categorias.isEmpty()) {
                List<String> categoriasLimpias = categorias.stream()
                        .filter(categoriaActual -> categoriaActual != null && !categoriaActual.isBlank())
                        .map(categoriaActual -> categoriaActual.trim().toLowerCase())
                        .toList();

                if (!categoriasLimpias.isEmpty()) {
                    List<jakarta.persistence.criteria.Predicate> predicatesCategoria = new ArrayList<>();

                    for (String categoriaActual : categoriasLimpias) {
                        predicatesCategoria.add(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(categoriaJoin.get("nombre")),
                                        "%" + categoriaActual + "%"
                                )
                        );
                    }

                    predicates.add(
                            criteriaBuilder.or(
                                    predicatesCategoria.toArray(new jakarta.persistence.criteria.Predicate[0])
                            )
                    );
                }
            }

            if (busqueda != null && !busqueda.isBlank()) {
                String termino = "%" + busqueda.trim().toLowerCase() + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                criteriaBuilder.coalesce(root.get("nombre"), "")
                                        ),
                                        termino
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                criteriaBuilder.coalesce(categoriaJoin.get("nombre"), "")
                                        ),
                                        termino
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                criteriaBuilder.coalesce(tiendaJoin.get("nombre"), "")
                                        ),
                                        termino
                                )
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Producto> productosPage = productoRepository.findAll(specification, pageable);

        List<Long> productoIds = productosPage.getContent()
                .stream()
                .map(Producto::getId)
                .toList();

        Map<Long, List<ProductoTallaStock>> tallasPorProducto = productoIds.isEmpty()
                ? Map.of()
                : productoTallaStockRepository.findByProductoIdIn(productoIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getProducto().getId()));

        Map<Long, List<ProductoImagen>> imagenesPorProducto = productoIds.isEmpty()
                ? Map.of()
                : productoImagenRepository.findByProductoIdInOrderByProductoIdAscOrdenAsc(productoIds)
                .stream()
                .collect(Collectors.groupingBy(imagen -> imagen.getProducto().getId()));

        List<ProductoListadoDTO> productosDTO = productosPage.getContent()
                .stream()
                .map(producto -> convertirAProductoListadoDTO(
                        producto,
                        tallasPorProducto.get(producto.getId()),
                        imagenesPorProducto.get(producto.getId())
                ))
                .toList();

        return new ProductoPageResponseDTO(
                productosDTO,
                productosPage.getNumber(),
                productosPage.getTotalPages(),
                productosPage.getTotalElements(),
                productosPage.isLast()
        );
    }

    public List<String> obtenerCategoriasPorTienda(String tienda) {
        if (tienda == null || tienda.isBlank()) {
            return List.of();
        }

        return productoRepository.findCategoriasDistintasPorTienda(tienda.trim());
    }

    private Sort obtenerOrdenProductos(String orden) {
        if (orden == null || orden.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (orden) {
            case "precioAsc" -> Sort.by(Sort.Direction.ASC, "precio");
            case "precioDesc" -> Sort.by(Sort.Direction.DESC, "precio");
            case "nombreAsc" -> Sort.by(Sort.Direction.ASC, "nombre");
            case "nombreDesc" -> Sort.by(Sort.Direction.DESC, "nombre");
            case "recientes" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    private ProductoListadoDTO convertirAProductoListadoDTO(Producto producto,
                                                            List<ProductoTallaStock> tallaStocks,
                                                            List<ProductoImagen> imagenes) {
        CategoriaSimpleDTO categoriaDTO = null;

        if (producto.getCategoria() != null) {
            categoriaDTO = new CategoriaSimpleDTO(
                    producto.getCategoria().getId(),
                    producto.getCategoria().getNombre()
            );
        }

        TiendaSimpleDTO tiendaDTO = null;

        if (producto.getTienda() != null) {
            tiendaDTO = new TiendaSimpleDTO(
                    producto.getTienda().getId(),
                    producto.getTienda().getNombre(),
                    producto.getTienda().getUrl()
            );
        }

        return new ProductoListadoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getUrlImagen(),
                producto.getUrlProducto(),
                producto.getSeccion(),
                categoriaDTO,
                tiendaDTO,
                construirTallasCompletas(tallaStocks),
                construirImagenesProducto(imagenes)
        );
    }

    private List<ProductoImagenResponseDTO> construirImagenesProducto(List<ProductoImagen> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) {
            return List.of();
        }

        return imagenes.stream()
                .filter(imagen -> imagen.getUrlImagen() != null && !imagen.getUrlImagen().isBlank())
                .sorted(Comparator.comparingInt(ProductoImagen::getOrden))
                .map(imagen -> new ProductoImagenResponseDTO(
                        imagen.getId(),
                        imagen.getUrlImagen(),
                        imagen.getOrden()
                ))
                .toList();
    }

    private List<ProductoTallaStockResponseDTO> construirTallasCompletas(List<ProductoTallaStock> tallaStocks) {
        Map<Talla, ProductoTallaStock> tallasExistentes = tallaStocks == null
                ? Map.of()
                : tallaStocks.stream()
                .collect(Collectors.toMap(
                        ProductoTallaStock::getTalla,
                        item -> item,
                        (item1, item2) -> item1
                ));

        return Arrays.stream(Talla.values())
                .map(tallaEnum -> {
                    ProductoTallaStockResponseDTO dto = new ProductoTallaStockResponseDTO();
                    dto.setTalla(tallaEnum);

                    ProductoTallaStock tallaEncontrada = tallasExistentes.get(tallaEnum);

                    if (tallaEncontrada != null) {
                        dto.setStock(tallaEncontrada.getStock());
                    } else {
                        dto.setStock(0);
                    }

                    return dto;
                })
                .toList();
    }

    public void asignarTallaStock(ProductoTallaStockDTO dto){
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("producto no encontrado"));

        Optional<ProductoTallaStock> existente =
                productoTallaStockRepository.findByProductoIdAndTalla(dto.getProductoId(), dto.getTalla());

        if (existente.isPresent()) {
            ProductoTallaStock productoTallaStockExistente = existente.get();
            productoTallaStockExistente.setStock(dto.getStock());
            productoTallaStockRepository.save(productoTallaStockExistente);
        } else {
            ProductoTallaStock productoTallaStock = new ProductoTallaStock();
            productoTallaStock.setProducto(producto);
            productoTallaStock.setTalla(dto.getTalla());
            productoTallaStock.setStock(dto.getStock());

            productoTallaStockRepository.save(productoTallaStock);
        }
    }

    public List<ProductoTallaStockResponseDTO> obtenerTallasStockPorProducto(Long productoId) {
        List<ProductoTallaStock> lista = productoTallaStockRepository.findByProductoId(productoId);

        return Arrays.stream(Talla.values())
                .map(tallaEnum -> {
                    ProductoTallaStockResponseDTO dto = new ProductoTallaStockResponseDTO();
                    dto.setTalla(tallaEnum);

                    ProductoTallaStock tallaEncontrada = lista.stream()
                            .filter(item -> item.getTalla() == tallaEnum)
                            .findFirst()
                            .orElse(null);

                    if (tallaEncontrada != null) {
                        dto.setStock(tallaEncontrada.getStock());
                    } else {
                        dto.setStock(0);
                    }

                    return dto;
                })
                .toList();
    }
}