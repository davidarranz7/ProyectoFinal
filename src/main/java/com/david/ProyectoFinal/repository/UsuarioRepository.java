package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Rol;
import com.david.ProyectoFinal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByEmail(String email);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByNombreIgnoreCase(String nombre);

    List<Usuario> findByRol(Rol rol);
}
