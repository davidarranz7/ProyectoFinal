package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.service.CarritoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController/// indicamos que es un controllador
@RequestMapping("/carrito")/// ruta base para todas las operaciones relacionadas con el carrito
public class CarritoController {

    /// ponemos las dependencias
    private final CarritoService carritoService;

    /// inyectamos el servicio a través del constructor
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping("/agregar")
    /// ruta para agregar un producto al carrito
    public ItemCarrito agregarproducto(@RequestParam Long usuarioId,
                                       @RequestParam Long productoId,
                                       @RequestParam Integer cantidad) {
        return carritoService.agregarProducto(usuarioId, productoId, cantidad);
    }

    @GetMapping("/usuario/{usuarioId}")
    /// ruta para obtener el carrito de un usuario
    public List<ItemCarrito> obtenerItemsDelCarrito(@PathVariable Long usuarioId) {
        return carritoService.obtenerItemsDelCarrito(usuarioId);
    }

    @DeleteMapping("/eliminar")
    /// ruta para eliminar un producto del carrito
    public void eliminarProducto(@RequestParam Long usuarioId,
                                 @RequestParam Long productoId) {
        carritoService.eliminarProducto(usuarioId, productoId);
    }
}
