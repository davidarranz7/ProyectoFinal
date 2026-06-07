package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.ItemCarrito;
import com.david.ProyectoFinal.model.Talla;
import com.david.ProyectoFinal.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    private void comprobarAccesoUsuario(Long usuarioId, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder al carrito de otro usuario");
        }
    }

    @PostMapping("/agregar")/// ruta para agregar un producto al carrito
    public ItemCarrito agregarproducto(@RequestParam Long usuarioId,
                                       @RequestParam Long productoId,
                                       @RequestParam String talla,
                                       @RequestParam Integer cantidad,
                                       HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return carritoService.agregarProducto(usuarioId, productoId, convertirTalla(talla), cantidad);
    }

    @GetMapping("/usuario/{usuarioId}")/// ruta para obtener el carrito de un usuario
    public List<ItemCarrito> obtenerItemsDelCarrito(@PathVariable Long usuarioId,
                                                    HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return carritoService.obtenerItemsDelCarrito(usuarioId);
    }

    @DeleteMapping("/eliminar")/// ruta para eliminar un producto del carrito
    public void eliminarProducto(@RequestParam Long usuarioId,
                                 @RequestParam Long productoId,
                                 @RequestParam String talla,
                                 HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        carritoService.eliminarProducto(usuarioId, productoId, convertirTalla(talla));
    }

    @PutMapping("/actualizar-cantidad")/// ruta para actualizar la cantidad de un producto en el carrito
    public ItemCarrito actulizarCantidad(@RequestParam Long usuarioId,
                                         @RequestParam Long productoId,
                                         @RequestParam String talla,
                                         @RequestParam Integer nuevaCantidad,
                                         HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return carritoService.actualizarCantidad(usuarioId, productoId, convertirTalla(talla), nuevaCantidad);
    }

    @GetMapping("/total/{usuarioId}")/// ruta para obtener el total del carrito de un usuario
    public BigDecimal caclularTotal(@PathVariable Long usuarioId,
                                    HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return carritoService.calcularTotal(usuarioId);
    }

    @DeleteMapping("/vaciar")/// ruta para vaciar el carrito de un usuario
    public void vaciarCarrito(@RequestParam Long usuarioId,
                              HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        carritoService.vaciarCarrito(usuarioId);
    }

    @PutMapping("/cambiar-talla")
    public ItemCarrito cambiarTalla(@RequestParam Long usuarioId,
                                    @RequestParam Long productoId,
                                    @RequestParam String tallaActual,
                                    @RequestParam String nuevaTalla,
                                    HttpSession session) {
        comprobarAccesoUsuario(usuarioId, session);
        return carritoService.cambiarTalla(
                usuarioId,
                productoId,
                convertirTalla(tallaActual),
                convertirTalla(nuevaTalla)
        );
    }

    private Talla convertirTalla(String talla) {
        try {
            return Talla.fromJson(talla);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talla no valida");
        }
    }

}
