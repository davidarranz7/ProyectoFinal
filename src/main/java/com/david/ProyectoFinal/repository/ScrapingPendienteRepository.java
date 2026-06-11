package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.EstadoScrapingPendiente;
import com.david.ProyectoFinal.model.ScrapingPendiente;
import com.david.ProyectoFinal.model.TipoScrapingPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapingPendienteRepository extends JpaRepository<ScrapingPendiente, Long> {

    List<ScrapingPendiente> findTop3ByEstadoOrderByFechaCreacionAsc(EstadoScrapingPendiente estado);

    List<ScrapingPendiente> findTop5ByEstadoOrderByFechaCreacionAsc(EstadoScrapingPendiente estado);

    Optional<ScrapingPendiente> findFirstByTipoAndEstadoOrderByFechaCreacionAsc(
            TipoScrapingPendiente tipo,
            EstadoScrapingPendiente estado
    );

    List<ScrapingPendiente> findByTipoAndEstado(TipoScrapingPendiente tipo, EstadoScrapingPendiente estado);

    long countByEstado(EstadoScrapingPendiente estado);
}
