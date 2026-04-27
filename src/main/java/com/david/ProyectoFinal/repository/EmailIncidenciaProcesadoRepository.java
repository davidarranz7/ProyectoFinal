package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.EmailIncidenciaProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailIncidenciaProcesadoRepository extends JpaRepository<EmailIncidenciaProcesado, Long> {

    boolean existsByMessageIdEmail(String messageIdEmail);
}