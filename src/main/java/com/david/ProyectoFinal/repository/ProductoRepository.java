package com.david.ProyectoFinal.repository;

import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Optional<Producto> findByUrlProducto(String urlProducto);

    List<Producto> findByTiendaNombre(String nombre);

    @Query("""
        SELECT p
        FROM Producto p
        JOIN p.tienda t
        WHERE LOWER(t.nombre) = LOWER(:nombreTienda)
        AND (p.disponibleCatalogo = true OR p.disponibleCatalogo IS NULL)
        """)
    List<Producto> findDisponiblesCatalogoPorTienda(@Param("nombreTienda") String nombreTienda);

    @Modifying
    @Query("""
        UPDATE Producto p
        SET p.disponibleCatalogo = true
        WHERE p.disponibleCatalogo IS NULL
        """)
    int marcarDisponibilidadCatalogoNulaComoTrue();

    @Query("""
        SELECT DISTINCT c.nombre
        FROM Producto p
        JOIN p.categoria c
        WHERE p.disponibleCatalogo = true
        ORDER BY c.nombre ASC
        """)
    List<String> findCategoriasDistintas();

    @Query("""
        SELECT DISTINCT c.nombre
        FROM Producto p
        JOIN p.categoria c
        JOIN p.tienda t
        WHERE LOWER(t.nombre) = LOWER(:nombreTienda)
        AND p.disponibleCatalogo = true
        ORDER BY c.nombre ASC
        """)
    List<String> findCategoriasDistintasPorTienda(@Param("nombreTienda") String nombreTienda);

    @Query("""
        SELECT DISTINCT c.nombre
        FROM Producto p
        JOIN p.categoria c
        WHERE p.seccion IN :secciones
        AND p.disponibleCatalogo = true
        ORDER BY c.nombre ASC
        """)
    List<String> findCategoriasDistintasPorSecciones(@Param("secciones") List<Seccion> secciones);

    @Query("""
        SELECT DISTINCT c.nombre
        FROM Producto p
        JOIN p.categoria c
        JOIN p.tienda t
        WHERE LOWER(t.nombre) = LOWER(:nombreTienda)
        AND p.seccion IN :secciones
        AND p.disponibleCatalogo = true
        ORDER BY c.nombre ASC
        """)
    List<String> findCategoriasDistintasPorTiendaYSecciones(
            @Param("nombreTienda") String nombreTienda,
            @Param("secciones") List<Seccion> secciones
    );
}
