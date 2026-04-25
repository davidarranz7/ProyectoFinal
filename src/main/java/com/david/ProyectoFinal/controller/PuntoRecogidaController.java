package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.PuntoRecogidaRequestDTO;
import com.david.ProyectoFinal.dto.PuntoRecogidaResponseDTO;
import com.david.ProyectoFinal.service.PuntoRecogidaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/puntos-recogida")
public class PuntoRecogidaController {

    private final PuntoRecogidaService puntoRecogidaService;

    public PuntoRecogidaController(PuntoRecogidaService puntoRecogidaService) {
        this.puntoRecogidaService = puntoRecogidaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PuntoRecogidaResponseDTO> obtenerTodos() {
        return puntoRecogidaService.obtenerTodos();
    }

    @GetMapping("/disponibles")
    public List<PuntoRecogidaResponseDTO> obtenerDisponibles() {
        return puntoRecogidaService.obtenerDisponibles();
    }

    @GetMapping("/disponibles/provincias")
    public List<String> obtenerProvinciasDisponibles() {
        return puntoRecogidaService.obtenerProvinciasDisponibles();
    }

    @GetMapping("/disponibles/provincia/{provincia}/ciudades")
    public List<String> obtenerCiudadesDisponiblesPorProvincia(@PathVariable String provincia) {
        return puntoRecogidaService.obtenerCiudadesDisponiblesPorProvincia(provincia);
    }

    @GetMapping("/disponibles/ciudad/{ciudad}")
    public List<PuntoRecogidaResponseDTO> obtenerDisponiblesPorCiudad(@PathVariable String ciudad) {
        return puntoRecogidaService.obtenerDisponiblesPorCiudad(ciudad);
    }

    @GetMapping("/disponibles/provincia/{provincia}/ciudad/{ciudad}")
    public List<PuntoRecogidaResponseDTO> obtenerDisponiblesPorProvinciaYCiudad(@PathVariable String provincia,
                                                                                @PathVariable String ciudad) {
        return puntoRecogidaService.obtenerDisponiblesPorProvinciaYCiudad(provincia, ciudad);
    }

    @GetMapping("/ciudad/{ciudad}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PuntoRecogidaResponseDTO> obtenerPorCiudad(@PathVariable String ciudad) {
        return puntoRecogidaService.obtenerPorCiudad(ciudad);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PuntoRecogidaResponseDTO obtenerPorId(@PathVariable Long id) {
        return puntoRecogidaService.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PuntoRecogidaResponseDTO crear(@RequestBody PuntoRecogidaRequestDTO dto) {
        return puntoRecogidaService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PuntoRecogidaResponseDTO actualizar(@PathVariable Long id,
                                               @RequestBody PuntoRecogidaRequestDTO dto) {
        return puntoRecogidaService.actualizar(id, dto);
    }

    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public PuntoRecogidaResponseDTO cambiarDisponibilidad(@PathVariable Long id,
                                                          @RequestParam Boolean disponible,
                                                          @RequestParam(required = false) String motivoNoDisponible) {
        return puntoRecogidaService.cambiarDisponibilidad(id, disponible, motivoNoDisponible);
    }
}