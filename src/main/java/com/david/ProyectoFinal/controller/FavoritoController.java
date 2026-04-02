package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.service.FavoritoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")/// Ruta base para todos los endpoints relacionados con favoritos
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public Favorito agregarFavortio(@RequestParam Long usuarioId,
                                    @RequestParam Long productoId){
        return favoritoService.agregarFavorito(usuarioId, productoId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Favorito> obtenerFavoritosDeUsuario(@PathVariable Long usuarioId){
        return favoritoService.obtenerFavoritosDeUsuario(usuarioId);/// Obtiene la lista de favoritos de un usuario
    }

    @DeleteMapping
    public void eliminarFavorito(@RequestParam Long usuarioId, @RequestParam Long productoId){
        favoritoService.eliminarFavorito(usuarioId,productoId);
    }
}
