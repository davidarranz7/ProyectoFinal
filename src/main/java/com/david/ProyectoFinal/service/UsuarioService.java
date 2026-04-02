package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.dto.ActualizarPerfilDTO;
import com.david.ProyectoFinal.dto.CambiarPasswordDTO;
import com.david.ProyectoFinal.dto.UsuarioPerfilDTO;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service/// INdica que esto es la logica de negocio
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario){
        if (usuarioRepository.existsByNombre(usuario.getNombre())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario obternerPorId(Long id){
        return usuarioRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id){
        usuarioRepository.deleteById(id);
    }

    public Usuario actutualizar(Long id, Usuario usuarioActualizado){
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if(usuario != null){
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setPassword(usuarioActualizado.getPassword());
            usuario.setRol(usuarioActualizado.getRol());;

            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public Usuario login(String nombre, String password) {
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getNombre().equals(nombre) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public UsuarioPerfilDTO obtenerPerfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return new UsuarioPerfilDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name()
        );
    }

    public UsuarioPerfilDTO actualizarPerfil(Long usuarioId, ActualizarPerfilDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre no puede estar vacío");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("El email no puede estar vacío");
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return new UsuarioPerfilDTO(
                usuarioActualizado.getId(),
                usuarioActualizado.getNombre(),
                usuarioActualizado.getEmail(),
                usuarioActualizado.getRol().name()
        );
    }

    public void cambiarPassword(Long usuarioId, CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getPasswordActual() == null || dto.getPasswordNueva() == null || dto.getConfirmarPassword() == null) {
            throw new RuntimeException("Todos los campos son obligatorios");
        }

        if (!usuario.getPassword().equals(dto.getPasswordActual())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }

        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new RuntimeException("La nueva contraseña y la confirmación no coinciden");
        }

        if (dto.getPasswordNueva().length() < 4) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 4 caracteres");
        }

        usuario.setPassword(dto.getPasswordNueva());
        usuarioRepository.save(usuario);
    }

}
