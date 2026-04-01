package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.repository.EstablecimientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstablecimientoService {

    private final EstablecimientoRepository establecimientoRepository;

    public EstablecimientoService(EstablecimientoRepository establecimientoRepository) {
        this.establecimientoRepository = establecimientoRepository;
    }

    public List<Establecimiento> obtenerPorTiendaYCiudad(String nombreTienda, String ciudad) {
        return establecimientoRepository.findByTiendaNombreAndCiudadIgnoreCase(nombreTienda, ciudad);
    }
}
