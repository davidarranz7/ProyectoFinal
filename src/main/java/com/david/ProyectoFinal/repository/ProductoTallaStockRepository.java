package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ProductoTallaStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoTallaStockRepository extends JpaRepository<ProductoTallaStock, Long> {

    List<ProductoTallaStock> findByProductoId(Long productoId);
}
