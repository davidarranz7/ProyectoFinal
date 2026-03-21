package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.model.Categoria;
import com.david.ProyectoFinal.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
     }

     public List<Categoria> obtenertodas(){
         return categoriaRepository.findAll();
     }

     public Categoria guardar(Categoria categoria){
         return categoriaRepository.save(categoria);
     }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

     public void eliminar(Long id){
         categoriaRepository.deleteById(id);
     }

     public Categoria actualizar(Long id, Categoria categoriaActualizada) {
         Categoria categoria = categoriaRepository.findById(id).orElse(null);

         if (categoria != null) {
             categoria.setNombre(categoriaActualizada.getNombre());
             return categoriaRepository.save(categoria);
         }
         return null;
     }
}
