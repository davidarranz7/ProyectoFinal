package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.PuntoRecogidaRequestDTO;
import com.david.ProyectoFinal.dto.PuntoRecogidaResponseDTO;
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

    public List<PuntoRecogidaResponseDTO> obtenerTodos() {
        return puntoRecogidaRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PuntoRecogidaResponseDTO> obtenerDisponibles() {
        return puntoRecogidaRepository.findByDisponibleTrue()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<String> obtenerProvinciasDisponibles() {
        return puntoRecogidaRepository.findByDisponibleTrue()
                .stream()
                .map(PuntoRecogida::getProvincia)
                .filter(provincia -> provincia != null && !provincia.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> obtenerCiudadesDisponiblesPorProvincia(String provincia) {
        return puntoRecogidaRepository.findByProvinciaIgnoreCaseAndDisponibleTrue(provincia)
                .stream()
                .map(PuntoRecogida::getCiudad)
                .filter(ciudad -> ciudad != null && !ciudad.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<PuntoRecogidaResponseDTO> obtenerDisponiblesPorCiudad(String ciudad) {
        return puntoRecogidaRepository.findByCiudadIgnoreCaseAndDisponibleTrue(ciudad)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PuntoRecogidaResponseDTO> obtenerDisponiblesPorProvinciaYCiudad(String provincia, String ciudad) {
        return puntoRecogidaRepository.findByProvinciaIgnoreCaseAndCiudadIgnoreCaseAndDisponibleTrue(provincia, ciudad)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PuntoRecogidaResponseDTO> obtenerPorCiudad(String ciudad) {
        return puntoRecogidaRepository.findByCiudadIgnoreCase(ciudad)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public PuntoRecogida obtenerEntidadPorId(Long id) {
        return puntoRecogidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de recogida no encontrado"));
    }

    public PuntoRecogidaResponseDTO obtenerPorId(Long id) {
        PuntoRecogida puntoRecogida = obtenerEntidadPorId(id);
        return convertirAResponseDTO(puntoRecogida);
    }

    public PuntoRecogidaResponseDTO crear(PuntoRecogidaRequestDTO dto) {
        validarPuntoRecogida(dto);

        PuntoRecogida puntoRecogida = new PuntoRecogida();
        puntoRecogida.setNombre(dto.getNombre().trim());
        puntoRecogida.setDireccion(dto.getDireccion().trim());
        puntoRecogida.setCiudad(dto.getCiudad().trim());
        puntoRecogida.setProvincia(dto.getProvincia().trim());

        if (dto.getDisponible() == null) {
            puntoRecogida.setDisponible(true);
        } else {
            puntoRecogida.setDisponible(dto.getDisponible());
        }

        if (Boolean.TRUE.equals(puntoRecogida.getDisponible())) {
            puntoRecogida.setMotivoNoDisponible(null);
        } else {
            puntoRecogida.setMotivoNoDisponible(
                    dto.getMotivoNoDisponible() != null ? dto.getMotivoNoDisponible().trim() : null
            );
        }

        PuntoRecogida guardado = puntoRecogidaRepository.save(puntoRecogida);
        return convertirAResponseDTO(guardado);
    }

    public PuntoRecogidaResponseDTO actualizar(Long id, PuntoRecogidaRequestDTO dto) {
        PuntoRecogida existente = obtenerEntidadPorId(id);

        validarPuntoRecogida(dto);

        existente.setNombre(dto.getNombre().trim());
        existente.setDireccion(dto.getDireccion().trim());
        existente.setCiudad(dto.getCiudad().trim());
        existente.setProvincia(dto.getProvincia().trim());

        PuntoRecogida actualizado = puntoRecogidaRepository.save(existente);
        return convertirAResponseDTO(actualizado);
    }

    public PuntoRecogidaResponseDTO cambiarDisponibilidad(Long id, Boolean disponible, String motivoNoDisponible) {
        PuntoRecogida puntoRecogida = obtenerEntidadPorId(id);

        puntoRecogida.setDisponible(disponible);

        if (Boolean.TRUE.equals(disponible)) {
            puntoRecogida.setMotivoNoDisponible(null);
        } else {
            puntoRecogida.setMotivoNoDisponible(
                    motivoNoDisponible != null ? motivoNoDisponible.trim() : null
            );
        }

        PuntoRecogida actualizado = puntoRecogidaRepository.save(puntoRecogida);
        return convertirAResponseDTO(actualizado);
    }

    private void validarPuntoRecogida(PuntoRecogidaRequestDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del punto de recogida es obligatorio");
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
    }

    private PuntoRecogidaResponseDTO convertirAResponseDTO(PuntoRecogida puntoRecogida) {
        return new PuntoRecogidaResponseDTO(
                puntoRecogida.getId(),
                puntoRecogida.getNombre(),
                puntoRecogida.getDireccion(),
                puntoRecogida.getCiudad(),
                puntoRecogida.getProvincia(),
                puntoRecogida.getDisponible(),
                puntoRecogida.getMotivoNoDisponible()
        );
    }
}