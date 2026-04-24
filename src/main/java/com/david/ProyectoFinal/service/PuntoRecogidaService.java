package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.model.PuntoRecogida;
import com.david.ProyectoFinal.repository.PuntoRecogidaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PuntoRecogidaService {

    private final PuntoRecogidaRepository puntoRecogidaRepository;

    public PuntoRecogidaService(PuntoRecogidaRepository puntoRecogidaRepository) {
        this.puntoRecogidaRepository = puntoRecogidaRepository;
    }

    public List<PuntoRecogida> obtenerTodos() {
        return puntoRecogidaRepository.findAll();
    }

    public List<PuntoRecogida> obtenerDisponibles() {
        return puntoRecogidaRepository.findByDisponibleTrue();
    }

    public List<PuntoRecogida> obtenerDisponiblesPorCiudad(String ciudad) {
        return puntoRecogidaRepository.findByCiudadIgnoreCaseAndDisponibleTrue(ciudad);
    }

    public List<PuntoRecogida> obtenerPorCiudad(String ciudad) {
        return puntoRecogidaRepository.findByCiudadIgnoreCase(ciudad);
    }

    public PuntoRecogida obtenerPorId(Long id) {
        return puntoRecogidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de recogida no encontrado"));
    }

    public PuntoRecogida crear(PuntoRecogida puntoRecogida) {
        validarPuntoRecogida(puntoRecogida);

        if (puntoRecogida.getDisponible() == null) {
            puntoRecogida.setDisponible(true);
        }

        if (Boolean.TRUE.equals(puntoRecogida.getDisponible())) {
            puntoRecogida.setMotivoNoDisponible(null);
        }

        return puntoRecogidaRepository.save(puntoRecogida);
    }

    public PuntoRecogida actualizar(Long id, PuntoRecogida datosActualizados) {
        PuntoRecogida existente = obtenerPorId(id);

        validarPuntoRecogida(datosActualizados);

        existente.setNombre(datosActualizados.getNombre());
        existente.setDireccion(datosActualizados.getDireccion());
        existente.setCiudad(datosActualizados.getCiudad());
        existente.setProvincia(datosActualizados.getProvincia());

        return puntoRecogidaRepository.save(existente);
    }

    public PuntoRecogida cambiarDisponibilidad(Long id, Boolean disponible, String motivoNoDisponible) {
        PuntoRecogida puntoRecogida = obtenerPorId(id);

        puntoRecogida.setDisponible(disponible);

        if (Boolean.TRUE.equals(disponible)) {
            puntoRecogida.setMotivoNoDisponible(null);
        } else {
            puntoRecogida.setMotivoNoDisponible(motivoNoDisponible);
        }

        return puntoRecogidaRepository.save(puntoRecogida);
    }

    private void validarPuntoRecogida(PuntoRecogida puntoRecogida) {
        if (puntoRecogida.getNombre() == null || puntoRecogida.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del punto de recogida es obligatorio");
        }

        if (puntoRecogida.getDireccion() == null || puntoRecogida.getDireccion().isBlank()) {
            throw new RuntimeException("La dirección es obligatoria");
        }

        if (puntoRecogida.getCiudad() == null || puntoRecogida.getCiudad().isBlank()) {
            throw new RuntimeException("La ciudad es obligatoria");
        }

        if (puntoRecogida.getProvincia() == null || puntoRecogida.getProvincia().isBlank()) {
            throw new RuntimeException("La provincia es obligatoria");
        }
    }
}