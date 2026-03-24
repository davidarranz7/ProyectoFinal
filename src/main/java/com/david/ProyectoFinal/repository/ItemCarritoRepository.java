package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByCarritoId(Long carritoId);///devuelve todos los items de un carrito

    Optional<ItemCarrito> findByCarritoIdAndProductoId(Long carritoId, Long productoId);///busca si ese producto ya está en ese carrito

    void deleteByCarritoIdAndProductoId(Long carritoId, Long productoId);///elimina un producto concreto del carrito
}
