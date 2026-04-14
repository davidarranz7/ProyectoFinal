package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.service.FavoritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/favoritos")/// Ruta base para todos los endpoints relacionados con favoritos
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    private void comprobarAccesoUsuario(Long usuarioId, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a los favoritos de otro usuario");
        }
    }

    @PostMapping
    public Favorito agregarFavortio(@RequestParam Long usuarioId,
                                    @RequestParam Long productoId,
                                    HttpSession session){
        comprobarAccesoUsuario(usuarioId, session);
        return favoritoService.agregarFavorito(usuarioId, productoId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Favorito> obtenerFavoritosDeUsuario(@PathVariable Long usuarioId,
                                                    HttpSession session){
        comprobarAccesoUsuario(usuarioId, session);
        return favoritoService.obtenerFavoritosDeUsuario(usuarioId);/// Obtiene la lista de favoritos de un usuario
    }

    @DeleteMapping
    public void eliminarFavorito(@RequestParam Long usuarioId,
                                 @RequestParam Long productoId,
                                 HttpSession session){
        comprobarAccesoUsuario(usuarioId, session);
        favoritoService.eliminarFavorito(usuarioId,productoId);
    }
}