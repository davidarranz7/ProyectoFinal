package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.PasswordResetToken;
import com.david.ProyectoFinal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /// Busca en la base de datos el token que viene en el enlace del correo. Lo usaremos cuando el usuario quiera cambiar la contraseña.
    Optional<PasswordResetToken> findByToken(String token);

    /// Borra tokens anteriores de ese usuario. Así evitamos que tenga varios enlaces activos a la vez
    void deleteByUsuario(Usuario usuario);
}