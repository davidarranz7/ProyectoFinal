package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);///Devuelve todos los favoritos de un usuario.

    Optional<Favorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);///Sirve para comprobar si ese producto ya está en favoritos de ese usuario.

    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);///Borra el favorito exacto de ese usuario con ese producto.

    void deleteByUsuarioId(Long usuarioId);
}
