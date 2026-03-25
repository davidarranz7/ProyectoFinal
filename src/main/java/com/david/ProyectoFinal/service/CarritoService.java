package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.model.Carrito;
import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.CarritoRepository;
import com.david.ProyectoFinal.repository.ItemCarritoRepository;
import com.david.ProyectoFinal.repository.ProductoRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    /// Ponemos ls dependecias

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public Carrito obtenerOCrearCarrito(Long usuarioId){

        /// busca si el usuario ya tiene carrito
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioId(usuarioId);

        /// si existe lo devuelve
        if (carritoExistente.isPresent()){
            return carritoExistente.get();
        }
        /// busca al susuario para asociarlo al carrito
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);

        /// si el usuario no existe, no se puede crear el carrito
        if (usuarioOptional.isEmpty()){
            return null;
        }

        /// Crea carrito nuevo
        Carrito nuevoCarrito = new Carrito();
        nuevoCarrito.setUsuario(usuarioOptional.get());
        nuevoCarrito.setFechaCreacion(LocalDateTime.now());

        return carritoRepository.save(nuevoCarrito);
    }

    public ItemCarrito agregarProducto (Long usuarioId, Long productoId, Integer cantidad){

        /// obtenemos o creamos el carrito del usuario
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        /// Si no existe el usuario se corta el proceso
        if (carrito == null){
            return null;
        }

        /// buscamos el producto
        Optional<Producto> productoOptional = productoRepository.findById(productoId);

        /// validamos si el producto existe
        if (productoOptional.isEmpty()){
            return null;
        }

        /// busacmos el producto en el carrito
        Optional<ItemCarrito> itemExistente = itemCarritoRepository.findByCarritoIdAndProductoId(carrito.getId(), productoId);

        /// si ya existe el producto  no creas otro solo aumentas la cantidad
        if (itemExistente.isPresent()){
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() +  cantidad);
            return  itemCarritoRepository.save(item);
        }

        /// si no existe el producto en el carrito, lo añades como nuevo item
        ItemCarrito nuevoItem = new ItemCarrito();
        nuevoItem.setCarrito(carrito);
        nuevoItem.setProducto(productoOptional.get());
        nuevoItem.setCantidad(cantidad);

        /// guardamos el nuevo item en la base de datos
        return  itemCarritoRepository.save(nuevoItem);
    }

    public List<ItemCarrito> obtenerItemsDelCarrito(Long usuarioId){

        /// obtenemos el carrito del usuario o lo creamos si no existe
        Carrito carrito = obtenerOCrearCarrito(usuarioId);


        /// si algo falla se detiene el proceso
        if (carrito == null){
            return List.of();
        }

        /// busca todos los ItemCarrito que pertenecen a ese carrito
        return itemCarritoRepository.findByCarritoId(carrito.getId());
    }

    @Transactional/// para eliminar de manera segura
    public void eliminarProducto (Long usuarioId, Long productoId){

        /// obtenemos el carrito del usuario o lo creamos si no existe
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        /// si algo falla se detiene el proceso
        if (carrito == null){
            return;
        }

        /// eliminamos el producto del carrito
        itemCarritoRepository.deleteByCarritoIdAndProductoId(carrito.getId(), productoId);
    }

    public BigDecimal calcularTotal(Long usuarioId){

        /// obtenemos los items del carrito del usuario
        List<ItemCarrito> items = obtenerItemsDelCarrito(usuarioId);

        /// nicializar el total en 0
        BigDecimal total = BigDecimal.ZERO;

        /// Recorremos los items
        for(ItemCarrito item : items){
            /// sacamos el precio y la cantidad
            BigDecimal precio = item.getProducto().getPrecio();
            Integer cantidad = item.getCantidad();

            /// Multiplicamos y sumamos
            total = total.add(precio.multiply(BigDecimal.valueOf(cantidad)));
        }

        return total;
    }

    @Transactional/// para eliminar de manera segura
    public void vaciarCarrito(Long usuarioId){
        /// obtenemos el carrito del usuario o lo creamos si no existe
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        /// si algo falla se detiene el proceso
        if (carrito == null){
            return;
        }
        /// buscamos todos los ItemCarrito que pertenecen a ese carrito
        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(carrito.getId());
        /// eliminamos todos los items del carrito
        itemCarritoRepository.deleteAll();
    }

}
