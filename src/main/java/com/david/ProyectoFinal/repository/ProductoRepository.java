package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    java.util.Optional<Producto> findByUrlProducto(String urlProducto);

}
