package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos(){
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto){
        return productoRepository.save(producto);
    }

    public Producto obternerPorId(Long id){
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
}
