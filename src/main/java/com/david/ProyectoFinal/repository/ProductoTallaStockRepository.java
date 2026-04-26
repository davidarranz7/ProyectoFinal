package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ProductoTallaStock;
import com.david.ProyectoFinal.model.Talla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoTallaStockRepository extends JpaRepository<ProductoTallaStock, Long> {

    List<ProductoTallaStock> findByProductoId(Long productoId);

    Optional<ProductoTallaStock> findByProductoIdAndTalla(Long productoId, Talla talla);

    List<ProductoTallaStock> findByProductoIdIn(List<Long> productoIds);
}
