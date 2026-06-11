package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.EstadoScrapingEjecucion;
import com.david.ProyectoFinal.model.ScrapingEjecucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapingEjecucionRepository extends JpaRepository<ScrapingEjecucion, Long> {

    Optional<ScrapingEjecucion> findTopByOrderByFechaInicioDesc();

    List<ScrapingEjecucion> findTop5ByOrderByFechaInicioDesc();

    boolean existsByEstado(EstadoScrapingEjecucion estado);
}
