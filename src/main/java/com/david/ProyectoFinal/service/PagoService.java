package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.PagoResponseDTO;
import com.david.ProyectoFinal.dto.PagoRequestDTO;

public interface PagoService {

    PagoResponseDTO procesarPago(PagoRequestDTO dto);
}
