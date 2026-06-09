package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.SuscripcionPushUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionPushUsuarioRepository extends JpaRepository<SuscripcionPushUsuario, Long> {

    Optional<SuscripcionPushUsuario> findByEndpoint(String endpoint);

    List<SuscripcionPushUsuario> findByUsuarioIdAndActivaTrue(Long usuarioId);

    long countByUsuarioIdAndActivaTrue(Long usuarioId);
}
