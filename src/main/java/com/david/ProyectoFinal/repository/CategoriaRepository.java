package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
