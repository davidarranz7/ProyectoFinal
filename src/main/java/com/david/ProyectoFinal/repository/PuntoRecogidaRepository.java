package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.PuntoRecogida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuntoRecogidaRepository extends JpaRepository<PuntoRecogida, Long> {

    List<PuntoRecogida> findByCiudadIgnoreCaseAndDisponibleTrue(String ciudad);

    List<PuntoRecogida> findByDisponibleTrue();

    List<PuntoRecogida> findByCiudadIgnoreCase(String ciudad);

    List<PuntoRecogida> findByProvinciaIgnoreCaseAndDisponibleTrue(String provincia);

    List<PuntoRecogida> findByProvinciaIgnoreCaseAndCiudadIgnoreCaseAndDisponibleTrue(String provincia, String ciudad);
}