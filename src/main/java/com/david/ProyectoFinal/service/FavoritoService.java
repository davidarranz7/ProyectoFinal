package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.model.Favorito;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.FavoritoRepository;
import com.david.ProyectoFinal.repository.ProductoRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {

    /// Dependecias

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    /// Constructor para inyectar las dependencias

    public FavoritoService(FavoritoRepository favoritoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public Favorito agregarFavorito(Long usuarioId,Long productoId){

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
        Optional<Producto> productoOptional = productoRepository.findById(productoId);

        if(usuarioOptional.isEmpty() || productoOptional.isEmpty()){
            return null;/// Si no existe el usuario o el producto, devuelve null
        }
        Optional<Favorito> favoritoExiste = favoritoRepository.findByUsuarioIdAndProductoId(usuarioId,productoId);

        if(favoritoExiste.isPresent()){
            return favoritoExiste.get();/// Si el favorito ya existe, devuelve el favorito existente
        }

        if (!productoDisponibleEnCatalogo(productoOptional.get())) {
            throw new RuntimeException("El producto ya no esta disponible");
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuarioOptional.get());
        favorito.setProducto(productoOptional.get());
        favorito.setFechaAgregado(LocalDateTime.now());

        return favoritoRepository.save(favorito);
    }

    public List<Favorito> obtenerFavoritosDeUsuario(Long usuarioId){
        return favoritoRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(favorito -> productoDisponibleEnCatalogo(favorito == null ? null : favorito.getProducto()))
                .toList();
    }
    @Transactional
    public void eliminarFavorito(Long usuarioId, Long productoId){
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId,productoId);/// Elimina el favorito específico de ese usuario con ese producto
    }


    private boolean productoDisponibleEnCatalogo(Producto producto) {
        return producto != null && !Boolean.FALSE.equals(producto.getDisponibleCatalogo());
    }
}
