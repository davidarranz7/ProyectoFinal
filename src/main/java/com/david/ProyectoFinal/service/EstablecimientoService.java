package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.ActualizarEstablecimientoDTO;
import com.david.ProyectoFinal.dto.CrearEstablecimientoDTO;
import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.repository.EstablecimientoRepository;
import com.david.ProyectoFinal.repository.TiendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstablecimientoService {

    private final EstablecimientoRepository establecimientoRepository;
    private final TiendaRepository tiendaRepository;

    public EstablecimientoService(EstablecimientoRepository establecimientoRepository,
                                  TiendaRepository tiendaRepository) {
        this.establecimientoRepository = establecimientoRepository;
        this.tiendaRepository = tiendaRepository;
    }

    public List<Establecimiento> obtenerPorTiendaYCiudad(String nombreTienda, String ciudad) {
        return establecimientoRepository.findByTiendaNombreAndCiudadIgnoreCaseAndDisponibleTrue(nombreTienda, ciudad);
    }

    public List<Establecimiento> obtenerTodos() {
        return establecimientoRepository.findAll();
    }

    public Establecimiento obtenerPorId(Long id) {
        return establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));
    }

    public Establecimiento cambiarDisponibilidad(Long id, Boolean disponible, String motivoNoDisponible) {
        Establecimiento establecimiento = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));

        establecimiento.setDisponible(disponible);

        if (Boolean.TRUE.equals(disponible)) {
            establecimiento.setMotivoNoDisponible(null);
        } else {
            establecimiento.setMotivoNoDisponible(motivoNoDisponible);
        }

        return establecimientoRepository.save(establecimiento);
    }

    public Establecimiento crearEstablecimiento(CrearEstablecimientoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del establecimiento es obligatorio");
        }

        if (dto.getDireccion() == null || dto.getDireccion().isBlank()) {
            throw new RuntimeException("La dirección es obligatoria");
        }

        if (dto.getCiudad() == null || dto.getCiudad().isBlank()) {
            throw new RuntimeException("La ciudad es obligatoria");
        }

        if (dto.getProvincia() == null || dto.getProvincia().isBlank()) {
            throw new RuntimeException("La provincia es obligatoria");
        }

        if (dto.getNombreTienda() == null || dto.getNombreTienda().isBlank()) {
            throw new RuntimeException("El nombre de la tienda es obligatorio");
        }

        Tienda tienda = tiendaRepository.findByNombre(dto.getNombreTienda())
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setNombre(dto.getNombre().trim());
        establecimiento.setDireccion(dto.getDireccion().trim());
        establecimiento.setCiudad(dto.getCiudad().trim());
        establecimiento.setProvincia(dto.getProvincia().trim());
        establecimiento.setTienda(tienda);

        if (dto.getDisponible() == null) {
            establecimiento.setDisponible(true);
            establecimiento.setMotivoNoDisponible(null);
        } else {
            establecimiento.setDisponible(dto.getDisponible());

            if (Boolean.TRUE.equals(dto.getDisponible())) {
                establecimiento.setMotivoNoDisponible(null);
            } else {
                establecimiento.setMotivoNoDisponible(dto.getMotivoNoDisponible());
            }
        }

        return establecimientoRepository.save(establecimiento);
    }

    public Establecimiento actualizarEstablecimiento(Long id, ActualizarEstablecimientoDTO dto) {
        Establecimiento establecimiento = establecimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del establecimiento es obligatorio");
        }

        if (dto.getDireccion() == null || dto.getDireccion().isBlank()) {
            throw new RuntimeException("La dirección es obligatoria");
        }

        if (dto.getCiudad() == null || dto.getCiudad().isBlank()) {
            throw new RuntimeException("La ciudad es obligatoria");
        }

        if (dto.getProvincia() == null || dto.getProvincia().isBlank()) {
            throw new RuntimeException("La provincia es obligatoria");
        }

        if (dto.getNombreTienda() == null || dto.getNombreTienda().isBlank()) {
            throw new RuntimeException("El nombre de la tienda es obligatorio");
        }

        Tienda tienda = tiendaRepository.findByNombre(dto.getNombreTienda())
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        establecimiento.setNombre(dto.getNombre().trim());
        establecimiento.setDireccion(dto.getDireccion().trim());
        establecimiento.setCiudad(dto.getCiudad().trim());
        establecimiento.setProvincia(dto.getProvincia().trim());
        establecimiento.setTienda(tienda);

        return establecimientoRepository.save(establecimiento);
    }
}