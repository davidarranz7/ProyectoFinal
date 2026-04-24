package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.PuntoRecogida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuntoRecogidaRepository extends JpaRepository<PuntoRecogida, Long> {

    /// para checkout: cargar solo los puntos disponibles de una ciudad
    List<PuntoRecogida> findByCiudadIgnoreCaseAndDisponibleTrue(String ciudad);
    /// para filtrar todos los que estan disponibles
    List<PuntoRecogida> findByDisponibleTrue();
    /// útil para admin o para otras consultas sin filtrar disponibilidad
    List<PuntoRecogida> findByCiudadIgnoreCase(String ciudad);
}