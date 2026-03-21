package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.model.Tienda;
import com.david.ProyectoFinal.repository.TiendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TiendaService {

    private final TiendaRepository tiendaRepository;

    public TiendaService(TiendaRepository tiendaRepository){
        this.tiendaRepository = tiendaRepository;
    }

    public List<Tienda> obtenerTodas(){
        return tiendaRepository.findAll();
    }

    public Tienda guardar(Tienda tienda){
        return tiendaRepository.save(tienda);
    }

    public Tienda obtenerPorId(Long id) {
        return tiendaRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        tiendaRepository.deleteById(id);
    }

    public Tienda actualizar(Long id, Tienda tiendaActualizada) {
        Tienda tienda = tiendaRepository.findById(id).orElse(null);

        if (tienda != null) {
            tienda.setNombre(tiendaActualizada.getNombre());
            tienda.setUrl(tiendaActualizada.getUrl());
            return tiendaRepository.save(tienda);
        }
        return null;
    }

}
