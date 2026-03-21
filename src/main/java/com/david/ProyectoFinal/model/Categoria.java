package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity/// Convierte la clase en tabla
@Table(name = "categorias")/// nombre de la Tabla

public class Categoria {

    @Id/// Indica que es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    private String nombre;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos
    public Categoria() {
    }

    /// Constructor con parámetros-> Todos los necesarios de una Categoria

    public Categoria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /// Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
