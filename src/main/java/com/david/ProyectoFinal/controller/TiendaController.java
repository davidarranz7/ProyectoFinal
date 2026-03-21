package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.service.TiendaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tiendas")
public class TiendaController {

    private final TiendaService tiendaService;

    public TiendaController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @GetMapping
    public List<Tienda> obtenerTodas() {
        return tiendaService.obtenerTodas();
    }

    @PostMapping
    public Tienda crear(@RequestBody Tienda tienda) {
        return tiendaService.guardar(tienda);
    }

    @GetMapping("/{id}")
    public Tienda obtenerPorId(@PathVariable Long id) {
        return tiendaService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        tiendaService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Tienda actualizar(@PathVariable Long id, @RequestBody Tienda tienda) {
        return tiendaService.actualizar(id, tienda);
    }
}
