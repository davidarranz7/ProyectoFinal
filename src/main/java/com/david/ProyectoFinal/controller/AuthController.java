package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario usuario) {
        Usuario usuarioEncontrado = usuarioService.login(usuario.getNombre(), usuario.getPassword());

        if (usuarioEncontrado == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(usuarioEncontrado);
    }
}