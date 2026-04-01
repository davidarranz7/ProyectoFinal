package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.Establecimiento;
import com.david.ProyectoFinal.service.EstablecimientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
