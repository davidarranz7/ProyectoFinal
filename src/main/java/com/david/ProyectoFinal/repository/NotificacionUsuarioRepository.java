package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.NotificacionUsuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacionUsuarioRepository extends JpaRepository<NotificacionUsuario, Long> {

    List<NotificacionUsuario> findByUsuarioIdOrderByLeidaAscFechaCreacionDesc(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    Optional<NotificacionUsuario> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying
    @Query("""
        UPDATE NotificacionUsuario n
        SET n.leida = true, n.fechaLeida = :fechaLeida
        WHERE n.usuario.id = :usuarioId
        AND n.leida = false
        """)
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId, @Param("fechaLeida") LocalDateTime fechaLeida);
}
