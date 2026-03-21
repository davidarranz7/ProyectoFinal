package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
}
