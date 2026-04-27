package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.EstadoIncidencia;
import com.david.ProyectoFinal.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    Optional<Incidencia> findByCodigoSeguimiento(String codigoSeguimiento);

    List<Incidencia> findAllByOrderByFechaUltimaActualizacionDesc();

    List<Incidencia> findByEstadoIncidenciaOrderByFechaUltimaActualizacionDesc(EstadoIncidencia estadoIncidencia);
}