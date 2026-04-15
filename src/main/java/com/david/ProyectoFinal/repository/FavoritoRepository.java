package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.model.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);///Devuelve todos los favoritos de un usuario.

    Optional<Favorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);///Sirve para comprobar si ese producto ya está en favoritos de ese usuario.

    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);///Borra el favorito exacto de ese usuario con ese producto.

    void deleteByUsuarioId(Long usuarioId);

    @Query("""
       SELECT f.producto
       FROM Favorito f
       GROUP BY f.producto
       ORDER BY COUNT(f.producto) DESC
       """)
    List<Producto> findProductosMasFavoritos(Pageable pageable);
}
