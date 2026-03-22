package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.repository.CategoriaRepository;
import com.david.ProyectoFinal.repository.ProductoRepository;
import com.david.ProyectoFinal.repository.TiendaRepository;
import com.david.ProyectoFinal.scraper.gestor.GestorScraping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final GestorScraping gestorScraping;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, GestorScraping gestorScraping, TiendaRepository tiendaRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.gestorScraping = gestorScraping;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> obtenerTodos(){
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto){
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Long id){
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id){
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
        List<Producto> productosGuardados = new java.util.ArrayList<>();

        for (Producto producto : productosScrapeados) {

            Tienda tiendaScrapeada = producto.getTienda();

            if (tiendaScrapeada != null) {
                Optional<Tienda> tiendaExistente =
                        tiendaRepository.findByNombre(tiendaScrapeada.getNombre());

                if (tiendaExistente.isPresent()) {
                    producto.setTienda(tiendaExistente.get());
                } else {
                    producto.setTienda(tiendaRepository.save(tiendaScrapeada));
                }
            }

            Categoria categoriaScrapeada = producto.getCategoria();

            if (categoriaScrapeada != null) {
                Optional<Categoria> categoriaExistente =
                        categoriaRepository.findByNombre(categoriaScrapeada.getNombre());

                if (categoriaExistente.isPresent()) {
                    producto.setCategoria(categoriaExistente.get());
                } else {
                    producto.setCategoria(categoriaRepository.save(categoriaScrapeada));
                }
            }


                Optional<Producto> existente =
                    productoRepository.findByUrlProducto(producto.getUrlProducto());

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
        return productoRepository.findByTiendaNombre(nombreTienda);
    }
}
