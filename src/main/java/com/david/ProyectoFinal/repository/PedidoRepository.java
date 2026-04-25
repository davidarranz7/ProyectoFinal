package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.EstadoPedido;
import com.david.ProyectoFinal.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByUsuarioIdAndEstado(Long usuarioId, EstadoPedido estado);

    List<Pedido> findByEstado(EstadoPedido estado);

    Optional<Pedido> findByTokenConfirmacionEntrega(String tokenConfirmacionEntrega);

    @Modifying
    @Query("DELETE FROM Pedido p WHERE p.usuario.id = :usuarioId")
    void deleteByUsuarioId(Long usuarioId);
}