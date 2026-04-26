package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambiarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarUsuarioRequestDTO;

public interface RecuperacionCuentaService {

    /// Recibe el email o usuario, busca la cuenta y envía el correo con el enlace temporal.
    void solicitarRecuperacionPassword(RecuperarPasswordRequestDTO request);

    /// Recibe el token y las nuevas contraseñas, valida todo y cambia la contraseña del usuario.
    void cambiarPassword(CambiarPasswordRequestDTO request);

    /// Recibe el email o usuario, busca la cuenta y envía el correo con el enlace temporal para recuperar el nombre de usuario.
    void solicitarRecuperacionUsuario(RecuperarUsuarioRequestDTO request);
}