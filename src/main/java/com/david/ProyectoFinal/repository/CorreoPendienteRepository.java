package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.CorreoPendiente;
import com.david.ProyectoFinal.model.EstadoCorreoPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorreoPendienteRepository extends JpaRepository<CorreoPendiente, Long> {

    List<CorreoPendiente> findTop10ByEstadoOrderByFechaCreacionAsc(EstadoCorreoPendiente estado);
}
