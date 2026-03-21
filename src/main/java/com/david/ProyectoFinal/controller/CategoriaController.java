package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.service.CategoriaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public Object obtenerTodas(){
        return categoriaService.obtenertodas();
    }

    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria){
        return categoriaService.guardar(categoria);
    }

    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        return categoriaService.actualizar(id, categoria);
    }
}
