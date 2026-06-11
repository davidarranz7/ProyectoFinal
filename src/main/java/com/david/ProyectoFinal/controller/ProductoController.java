package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.ProductoPageResponseDTO;
import com.david.ProyectoFinal.dto.ProductoSeleccionStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockMasivoDTO;
import com.david.ProyectoFinal.dto.ProductoTallaStockResponseDTO;
import com.david.ProyectoFinal.dto.ResultadoScrapingDTO;
import com.david.ProyectoFinal.dto.EstadoScrapingAdminDTO;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Rol;
import com.david.ProyectoFinal.model.Seccion;
import com.david.ProyectoFinal.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> obtenerTodos(@RequestParam(required = false) Boolean incluirNoDisponibles,
                                       HttpSession session){
        if (permitirIncluirNoDisponibles(incluirNoDisponibles, session)) {
            return productoService.obtenerTodos();
        }

        return productoService.obtenerTodosDisponiblesCatalogo();
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
            @RequestParam(required = false) Boolean incluirNoDisponibles,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            HttpSession session
    ) {
        boolean incluirNoDisponiblesSeguro = permitirIncluirNoDisponibles(incluirNoDisponibles, session);

        System.out.println(
                "Petición catálogo -> tienda=" + tienda
                        + ", secciones=" + secciones
                        + ", categorias=" + categorias
                        + ", busqueda=" + busqueda
                        + ", orden=" + orden
                        + ", enOferta=" + enOferta
                        + ", incluirNoDisponibles=" + incluirNoDisponiblesSeguro
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
                incluirNoDisponiblesSeguro,
                page,
                size
        );
    }

    @GetMapping("/catalogo/categorias")
    public List<String> obtenerCategoriasCatalogo(
            @RequestParam(required = false) String tienda,
            @RequestParam(name = "seccion", required = false) List<Seccion> secciones,
            @RequestParam(required = false) Boolean incluirNoDisponibles,
            HttpSession session
    ) {
        return productoService.obtenerCategoriasCatalogo(
                tienda,
                secciones,
                permitirIncluirNoDisponibles(incluirNoDisponibles, session)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/catalogo/seleccion-sin-stock")
    public ResponseEntity<List<ProductoSeleccionStockDTO>> obtenerProductosSinStockParaSeleccion(
            @RequestParam(required = false) String tienda,
            @RequestParam(name = "seccion", required = false) List<Seccion> secciones,
            @RequestParam(name = "categoria", required = false) List<String> categorias,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Boolean incluirNoDisponibles
    ) {
        return ResponseEntity.ok(
                productoService.buscarProductosSinStockParaSeleccion(
                        tienda,
                        secciones,
                        categorias,
                        busqueda,
                        incluirNoDisponibles
                )
        );
    }


    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id,
                                 @RequestParam(required = false) Boolean incluirNoDisponibles,
                                 HttpSession session) {
        Producto producto = productoService.obtenerPorId(
                id,
                permitirIncluirNoDisponibles(incluirNoDisponibles, session)
        );

        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible");
        }

        return producto;
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
    @GetMapping("/scraping/estado")
    public ResponseEntity<EstadoScrapingAdminDTO> obtenerEstadoScrapingAdmin() {
        return ResponseEntity.ok(productoService.obtenerEstadoScrapingAdmin());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/talla-stock")
    public ResponseEntity<String> asignarTallaStock(@RequestBody ProductoTallaStockDTO dto){
        productoService.asignarTallaStock(dto);
        return ResponseEntity.ok("Talla y stock asignados correctamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/talla-stock/masivo")
    public ResponseEntity<String> asignarTallaStockMasivo(@RequestBody ProductoTallaStockMasivoDTO dto) {
        productoService.asignarTallaStockMasivo(dto);
        return ResponseEntity.ok("Stock masivo asignado correctamente");
    }

    @GetMapping("/{productoId}/talla-stock")
    public ResponseEntity<List<ProductoTallaStockResponseDTO>> obtenerTallasStockPorProducto(
            @PathVariable Long productoId,
            @RequestParam(required = false) Boolean incluirNoDisponibles,
            HttpSession session
    ) {
        return ResponseEntity.ok(
                productoService.obtenerTallasStockPorProducto(
                        productoId,
                        permitirIncluirNoDisponibles(incluirNoDisponibles, session)
                )
        );
    }

    private boolean permitirIncluirNoDisponibles(Boolean incluirNoDisponibles, HttpSession session) {
        if (!Boolean.TRUE.equals(incluirNoDisponibles) || session == null) {
            return false;
        }

        Object rol = session.getAttribute("usuarioRol");
        return rol == Rol.ADMIN;
    }

}
