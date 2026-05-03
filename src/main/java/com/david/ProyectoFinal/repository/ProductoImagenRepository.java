package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    List<ProductoImagen> findByProductoIdInOrderByProductoIdAscOrdenAsc(List<Long> productoIds);
}