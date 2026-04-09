package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.model.Talla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByCarritoId(Long carritoId);///devuelve todos los items de un carrito

    Optional<ItemCarrito> findByCarritoIdAndProductoIdAndTalla(Long carritoId, Long productoId, Talla talla);///busca si ese producto ya está en ese carrito

    void deleteByCarritoIdAndProductoIdAndTalla (Long carritoId, Long productoId, Talla talla);///elimina un producto concreto del carrito

    void deleteByCarritoId(Long carritoId);///elimina todos los items de un carrito

    Optional<ItemCarrito> findByCarritoUsuarioIdAndProductoIdAndTalla(Long usuarioId, Long productoId, Talla talla);

    void deleteByCarritoUsuarioId(Long usuarioId);
}
