package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

}
