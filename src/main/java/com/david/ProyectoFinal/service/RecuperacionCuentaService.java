package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambiarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.CorreoOperacionResponseDTO;
import com.david.ProyectoFinal.dto.RecuperarPasswordRequestDTO;
import com.david.ProyectoFinal.dto.RecuperarUsuarioRequestDTO;

public interface RecuperacionCuentaService {

    CorreoOperacionResponseDTO solicitarRecuperacionPassword(RecuperarPasswordRequestDTO request);

    void cambiarPassword(CambiarPasswordRequestDTO request);

    CorreoOperacionResponseDTO solicitarRecuperacionUsuario(RecuperarUsuarioRequestDTO request);
}
