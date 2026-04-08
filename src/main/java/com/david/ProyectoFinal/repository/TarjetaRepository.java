package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    List<Tarjeta> findByUsuarioId(Long usuarioId);
}