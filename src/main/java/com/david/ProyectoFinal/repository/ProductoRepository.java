package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByUrlProducto(String urlProducto);

    List<Producto> findByTiendaNombre(String nombre);

}
