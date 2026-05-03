package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.ProductoPageResponseDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockResponseDTO;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/populares")
    public List<Producto> obtenerProductosPopulares() {
        return productoService.obtenerProductosMasFavoritos(4);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Producto crear(@RequestBody Producto producto){
        return productoService.guardar(producto);
    }

    @GetMapping("/catalogo")
    public ProductoPageResponseDTO buscarProductosCatalogo(
            @RequestParam(required = false) String tienda,
            @RequestParam(name = "seccion", required = false) List<Seccion> secciones,
            @RequestParam(name = "categoria", required = false) List<String> categorias,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        System.out.println(
                "Petición catálogo -> tienda=" + tienda
                        + ", secciones=" + secciones
                        + ", categorias=" + categorias
                        + ", busqueda=" + busqueda
                        + ", orden=" + orden
                        + ", page=" + page
                        + ", size=" + size
        );

        return productoService.buscarProductos(tienda, secciones, categorias, busqueda, orden, page, size);
    }

    @GetMapping("/catalogo/categorias")
    public List<String> obtenerCategoriasCatalogo(
            @RequestParam String tienda
    ) {
        return productoService.obtenerCategoriasPorTienda(tienda);
    }


    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarPorId(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/total")
    public ResponseEntity<List<Producto>> scrapearProductos(){
        List<Producto> productos = productoService.scrapearYGuardar();
        return ResponseEntity.ok(productos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/zara")
    public ResponseEntity<List<Producto>> scrapearProductosZara() {
        List<Producto> productos = productoService.scrapearZaraYGuardar();
        return ResponseEntity.ok(productos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/bershka")
    public ResponseEntity<List<Producto>> scrapearProductosBershka() {
        List<Producto> productos = productoService.scrapearBershkaYGuardar();
        return ResponseEntity.ok(productos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/pullandbear")
    public ResponseEntity<List<Producto>> scrapearProductosPullAndBear() {
        List<Producto> productos = productoService.scrapearPullAndBearYGuardar();
        return ResponseEntity.ok(productos);
    }

    @PreAuthorize("hasRole('ADMIN')")
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
