package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.model.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);

    List<Favorito> findByProductoId(Long productoId);

    Optional<Favorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    void deleteByUsuarioId(Long usuarioId);

    @Query("""
       SELECT f.producto
       FROM Favorito f
       GROUP BY f.producto
       ORDER BY COUNT(f.producto) DESC
       """)
    List<Producto> findProductosMasFavoritos(Pageable pageable);
}
