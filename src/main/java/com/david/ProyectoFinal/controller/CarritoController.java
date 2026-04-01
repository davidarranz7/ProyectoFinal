package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.model.Talla;
import com.david.ProyectoFinal.service.CarritoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @PostMapping("/agregar")/// ruta para agregar un producto al carrito
    public ItemCarrito agregarproducto(@RequestParam Long usuarioId,
                                       @RequestParam Long productoId,
                                       @RequestParam Talla talla,
                                       @RequestParam Integer cantidad) {
        return carritoService.agregarProducto(usuarioId, productoId, talla, cantidad);
    }

    @GetMapping("/usuario/{usuarioId}")/// ruta para obtener el carrito de un usuario
    public List<ItemCarrito> obtenerItemsDelCarrito(@PathVariable Long usuarioId) {
        return carritoService.obtenerItemsDelCarrito(usuarioId);
    }

    @DeleteMapping("/eliminar")/// ruta para eliminar un producto del carrito
    public void eliminarProducto(@RequestParam Long usuarioId,
                                 @RequestParam Long productoId,
                                 @RequestParam Talla talla) {
        carritoService.eliminarProducto(usuarioId, productoId, talla);
    }

    @PutMapping("/actualizar-cantidad")/// ruta para actualizar la cantidad de un producto en el carrito
    public ItemCarrito actulizarCantidad(@RequestParam Long usuarioId,
                                         @RequestParam Long productoId,
                                         @RequestParam Talla talla,
                                         @RequestParam Integer nuevaCantidad) {
        return carritoService.actualizarCantidad(usuarioId, productoId, talla, nuevaCantidad);
    }

    @GetMapping("/total/{usuarioId}")/// ruta para obtener el total del carrito de un usuario
    public BigDecimal caclularTotal(@PathVariable Long usuarioId) {
        return carritoService.calcularTotal(usuarioId);
    }

    @DeleteMapping("/vaciar")/// ruta para vaciar el carrito de un usuario
    public void vaciarCarrito(@RequestParam Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
    }

    @PutMapping("/cambiar-talla")
    public ItemCarrito cambiarTalla(@RequestParam Long usuarioId,
                                    @RequestParam Long productoId,
                                    @RequestParam Talla tallaActual,
                                    @RequestParam Talla nuevaTalla) {
        return carritoService.cambiarTalla(usuarioId, productoId, tallaActual, nuevaTalla);
    }

}
