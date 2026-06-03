package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.ProductoPageResponseDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockResponseDTO;
import com.david.ProyectoFinal.dto.ResultadoScrapingDTO;
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
            @RequestParam(required = false) Boolean enOferta,
            @RequestParam(required = false) Boolean nuevaColeccion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        System.out.println(
                "Petición catálogo -> tienda=" + tienda
                        + ", secciones=" + secciones
                        + ", categorias=" + categorias
                        + ", busqueda=" + busqueda
                        + ", orden=" + orden
                        + ", enOferta=" + enOferta
                        + ", page=" + page
                        + ", size=" + size
        );

        return productoService.buscarProductos(
                tienda,
                secciones,
                categorias,
                busqueda,
                orden,
                enOferta,
                nuevaColeccion,
                page,
                size
        );
    }

    @GetMapping("/catalogo/categorias")
    public List<String> obtenerCategoriasCatalogo(
            @RequestParam(required = false) String tienda,
            @RequestParam(name = "seccion", required = false) List<Seccion> secciones
    ) {
        return productoService.obtenerCategoriasCatalogo(tienda, secciones);
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
    public ResponseEntity<ResultadoScrapingDTO> scrapearProductos() {
        ResultadoScrapingDTO resultado = productoService.scrapearYGuardarConResultado();
        return ResponseEntity.ok(resultado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/zara")
    public ResponseEntity<ResultadoScrapingDTO> scrapearProductosZara() {
        ResultadoScrapingDTO resultado = productoService.scrapearZaraYGuardarConResultado();
        return ResponseEntity.ok(resultado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/bershka")
    public ResponseEntity<ResultadoScrapingDTO> scrapearProductosBershka() {
        ResultadoScrapingDTO resultado = productoService.scrapearBershkaYGuardarConResultado();
        return ResponseEntity.ok(resultado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/scrapear/pullandbear")
    public ResponseEntity<ResultadoScrapingDTO> scrapearProductosPullAndBear() {
        ResultadoScrapingDTO resultado = productoService.scrapearPullAndBearYGuardarConResultado();
        return ResponseEntity.ok(resultado);
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
