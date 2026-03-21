package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {

    java.util.Optional<Tienda> findByNombre(String nombre);
}
