package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento,Long> {
    List<Establecimiento> findByTiendaNombreAndCiudadIgnoreCase(String nombreTienda, String ciudad);
}
