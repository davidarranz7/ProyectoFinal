package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.ProductoTallaStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockResponseDTO;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.service.ProductoService;
import org.springframework.http.ResponseEntity;
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
        return productoService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarPorId(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @PostMapping("/scrapear")
    public ResponseEntity<List<Producto>> scrapearProductos(){
        List<Producto> productos = productoService.scrapearYGuardar();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/tienda/{nombre}")
    public List<Producto> obtenerPorTienda(@PathVariable String nombre) {
        return productoService.obtenerPorTienda(nombre);
    }

    @PostMapping("/talla-stock")
    public ResponseEntity<String> asignarTallaStock(@RequestBody ProductoTallaStockDTO dto){
        productoService.asignarTallaStock(dto);
        return ResponseEntity.ok("Talla y stock asignados correctamente");
    }

    @GetMapping("/{productoId}/talla-stock")
    public ResponseEntity<List<ProductoTallaStockResponseDTO>> obtenerTallasStockPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(productoService.obtenerTallasStockPorProducto(productoId));
    }

}
