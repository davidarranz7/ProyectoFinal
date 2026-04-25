package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ConfirmacionEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmacionEntregaRepository extends JpaRepository<ConfirmacionEntrega, Long> {

    Optional<ConfirmacionEntrega> findByToken(String token);

    Optional<ConfirmacionEntrega> findByPedidoId(Long pedidoId);
}