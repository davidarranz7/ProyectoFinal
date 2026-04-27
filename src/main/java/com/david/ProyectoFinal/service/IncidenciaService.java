package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CrearIncidenciaRequestDTO;
import com.david.ProyectoFinal.dto.IncidenciaResponseDTO;

public interface IncidenciaService {

    /// Recibirá los datos del formulario público, creará la incidencia, guardará el primer mensaje y devolverá los datos principales al frontend
    IncidenciaResponseDTO crearIncidencia(CrearIncidenciaRequestDTO request);
}