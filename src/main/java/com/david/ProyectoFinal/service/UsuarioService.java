package com.david.ProyectoFinal.service;


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

}
