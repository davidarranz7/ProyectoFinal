package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Optional<Producto> findByUrlProducto(String urlProducto);

    List<Producto> findByTiendaNombre(String nombre);

}