package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.MensajeIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeIncidenciaRepository extends JpaRepository<MensajeIncidencia, Long> {

    List<MensajeIncidencia> findByIncidenciaIdOrderByFechaMensajeAsc(Long incidenciaId);
}