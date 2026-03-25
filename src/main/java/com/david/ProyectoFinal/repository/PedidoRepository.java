package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByusuarioId(Long usuarioId);
}
