package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.ActualizarPerfilDTO;
import com.david.ProyectoFinal.dto.CambiarPasswordDTO;
import com.david.ProyectoFinal.dto.UsuarioPerfilDTO;
import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.model.Pedido;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.service.FavoritoService;
import com.david.ProyectoFinal.service.PedidoService;
import com.david.ProyectoFinal.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final FavoritoService favoritoService;

    public UsuarioController(UsuarioService usuarioService, PedidoService pedidoService, FavoritoService favoritoService) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.favoritoService = favoritoService;
    }

    private void comprobarAccesoUsuario(Long id, HttpSession session) {
        Long usuarioIdSesion = (Long) session.getAttribute("usuarioId");

        if (usuarioIdSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión iniciada");
        }

        if (!usuarioIdSesion.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a los datos de otro usuario");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Usuario> obtenerTodos(){
        return usuarioService.obtenerTodos();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.guardar(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Usuario obtenerPorId(@PathVariable Long id) {
        return usuarioService.obternerPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return usuarioService.actutualizar(id, usuarioActualizado);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<UsuarioPerfilDTO> obtenerPerfil(@PathVariable Long id, HttpSession session) {
        comprobarAccesoUsuario(id, session);
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id,
                                              @RequestBody ActualizarPerfilDTO dto,
                                              HttpSession session) {
        comprobarAccesoUsuario(id, session);
        try {
            UsuarioPerfilDTO usuarioActualizado = usuarioService.actualizarPerfil(id, dto);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> cambiarPassword(@PathVariable Long id,
                                                  @RequestBody CambiarPasswordDTO dto,
                                                  HttpSession session) {
        comprobarAccesoUsuario(id, session);
        usuarioService.cambiarPassword(id, dto);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @GetMapping("/{id}/validar-nombre")
    public ResponseEntity<?> validarNombrePerfil(@PathVariable Long id,
                                                 @RequestParam String nombre,
                                                 HttpSession session) {
        comprobarAccesoUsuario(id, session);
        try {
            return ResponseEntity.ok(usuarioService.validarNombrePerfil(id, nombre));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/validar-email")
    public ResponseEntity<?> validarEmailPerfil(@PathVariable Long id,
                                                @RequestParam String email,
                                                HttpSession session) {
        comprobarAccesoUsuario(id, session);
        try {
            return ResponseEntity.ok(usuarioService.validarEmailPerfil(id, email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFotoPerfil(@PathVariable Long id,
                                             @RequestParam("foto") MultipartFile foto,
                                             @RequestParam("formaFotoPerfil") String formaFotoPerfil,
                                             HttpSession session) {
        comprobarAccesoUsuario(id, session);

        try {
            UsuarioPerfilDTO usuarioActualizado = usuarioService.subirFotoPerfil(id, foto, formaFotoPerfil);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/pedidos")
    public List<Pedido> obtenerPedidosDeUsuarioComoAdmin(@PathVariable Long id) {
        return pedidoService.obtenerPedidosPorUsuario(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/favoritos")
    public List<Favorito> obtenerFavoritosDeUsuarioComoAdmin(@PathVariable Long id) {
        return favoritoService.obtenerFavoritosDeUsuario(id);
    }
}
