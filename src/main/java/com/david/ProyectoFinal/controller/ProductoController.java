package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.service.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> obtenerTodos(){
        return productoService.obtenerTodos();
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto){
        return productoService.guardar(producto);
    }

    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id) {
        return productoService.obternerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarPorId(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @PutMapping
    public Producto actualizar(@RequestParam Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }
}
