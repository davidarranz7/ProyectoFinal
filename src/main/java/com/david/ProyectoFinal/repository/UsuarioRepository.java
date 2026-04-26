package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByNombre(String nombre);
    boolean existsByEmail(String email);

    /// validacion en tiempo real....

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByEmailIgnoreCase(String email);

    /// Busca un usuario por email sin importar mayúsculas o minúsculas. Devuelve el usuario si existe.
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /// Busca un usuario por nombre de usuario sin importar mayúsculas o minúsculas. Devuelve el usuario si existe.
    Optional<Usuario> findByNombreIgnoreCase(String nombre);

}
