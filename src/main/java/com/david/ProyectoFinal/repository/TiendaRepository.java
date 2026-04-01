package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {

    Optional<Tienda> findByNombre(String nombre);
}
