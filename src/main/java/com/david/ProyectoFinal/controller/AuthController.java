package com.david.ProyectoFinal.controller;

import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<Usuario> login(@RequestBody Usuario usuario, HttpSession session) {
        Usuario usuarioEncontrado = usuarioService.login(usuario.getNombre(), usuario.getPassword());

        if (usuarioEncontrado == null) {
            return ResponseEntity.status(401).build();
        }

        session.setAttribute("usuarioId", usuarioEncontrado.getId());
        session.setAttribute("nombreUsuario", usuarioEncontrado.getNombre());
        session.setAttribute("usuarioRol", usuarioEncontrado.getRol());

        return ResponseEntity.ok(usuarioEncontrado);
    }


    @GetMapping("/session")
    public ResponseEntity<?> obtenerSesion(HttpServletRequest request) {

        HttpSession session = request.getSession(false); // 🔥 CLAVE

        if (session == null) {
            return ResponseEntity.status(401).body("No hay sesión iniciada");
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        Object usuarioRol = session.getAttribute("usuarioRol");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No hay sesión iniciada");
        }

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("id", usuarioId);
            put("nombre", nombreUsuario);
            put("rol", usuarioRol);
        }});
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}