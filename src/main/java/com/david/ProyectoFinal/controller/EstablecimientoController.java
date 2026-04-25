package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.dto.ActualizarEstablecimientoDTO;
import com.david.ProyectoFinal.dto.CrearEstablecimientoDTO;
import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.service.EstablecimientoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/establecimientos")
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;

    public EstablecimientoController(EstablecimientoService establecimientoService) {
        this.establecimientoService = establecimientoService;
    }

    @GetMapping("/tienda/{nombreTienda}/ciudad/{ciudad}")
    public List<Establecimiento> obtenerPorTiendaYCiudad(@PathVariable String nombreTienda,
                                                         @PathVariable String ciudad) {
        return establecimientoService.obtenerPorTiendaYCiudad(nombreTienda, ciudad);
    }

    @GetMapping("/tienda/{nombreTienda}/provincias")
    public List<String> obtenerProvinciasDisponiblesPorTienda(@PathVariable String nombreTienda) {
        return establecimientoService.obtenerProvinciasDisponiblesPorTienda(nombreTienda);
    }

    @GetMapping("/tienda/{nombreTienda}/provincia/{provincia}/ciudades")
    public List<String> obtenerCiudadesDisponiblesPorTiendaYProvincia(@PathVariable String nombreTienda,
                                                                      @PathVariable String provincia) {
        return establecimientoService.obtenerCiudadesDisponiblesPorTiendaYProvincia(nombreTienda, provincia);
    }

    @GetMapping("/tienda/{nombreTienda}/provincia/{provincia}/ciudad/{ciudad}")
    public List<Establecimiento> obtenerPorTiendaProvinciaYCiudad(@PathVariable String nombreTienda,
                                                                  @PathVariable String provincia,
                                                                  @PathVariable String ciudad) {
        return establecimientoService.obtenerPorTiendaProvinciaYCiudad(nombreTienda, provincia, ciudad);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Establecimiento> obtenerTodos() {
        return establecimientoService.obtenerTodos();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Establecimiento crearEstablecimiento(@RequestBody CrearEstablecimientoDTO dto) {
        return establecimientoService.crearEstablecimiento(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disponibilidad")
    public Establecimiento cambiarDisponibilidad(@PathVariable Long id,
                                                 @RequestParam Boolean disponible,
                                                 @RequestParam(required = false) String motivoNoDisponible) {
        return establecimientoService.cambiarDisponibilidad(id, disponible, motivoNoDisponible);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Establecimiento actualizarEstablecimiento(@PathVariable Long id,
                                                     @RequestBody ActualizarEstablecimientoDTO dto) {
        return establecimientoService.actualizarEstablecimiento(id, dto);
    }
}