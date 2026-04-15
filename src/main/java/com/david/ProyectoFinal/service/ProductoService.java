package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.ProductoTallaStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockResponseDTO;
import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import com.david.ProyectoFinal.scraper.gestor.GestorScraping;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final GestorScraping gestorScraping;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoTallaStockRepository productoTallaStockRepository;
    private final FavoritoRepository favoritoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           GestorScraping gestorScraping,
                           TiendaRepository tiendaRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoTallaStockRepository productoTallaStockRepository,
                           FavoritoRepository favoritoRepository) {
        this.productoRepository = productoRepository;
        this.gestorScraping = gestorScraping;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoTallaStockRepository = productoTallaStockRepository;
        this.favoritoRepository = favoritoRepository;
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