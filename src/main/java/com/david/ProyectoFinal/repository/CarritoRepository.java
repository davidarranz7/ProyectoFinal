package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
