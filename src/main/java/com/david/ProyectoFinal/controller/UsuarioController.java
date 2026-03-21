package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> obtenerTodos(){
        return usuarioService.obtenerTodos();
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario){
        return usuarioService.guardar(usuario);
    }

    @GetMapping("/{id}")
    public Usuario obtenerPorId(@PathVariable Long id) {
        return usuarioService.obternerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return usuarioService.actutualizar(id, usuarioActualizado);
    }
}
