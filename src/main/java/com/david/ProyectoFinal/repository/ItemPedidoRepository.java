package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /// método personalizado para encontrar los items de un pedido específico
    List<ItemPedido> findByPedidoId(Long pedidoId);

    @Modifying
    @Query("DELETE FROM ItemPedido ip WHERE ip.pedido.usuario.id = :usuarioId")
    void deleteByPedidoUsuarioId(Long usuarioId);
}

