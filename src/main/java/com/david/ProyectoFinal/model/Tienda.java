package com.david.ProyectoFinal.model;


import jakarta.persistence.*;

@Entity/// Convierte la clase en tabla
@Table(name = "tiendas")/// nombre de la Tabla
public class Tienda {

    @Id/// Indica que el campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)/// Genera el valor automáticamente
    private Long id;

    private String nombre;
    private String url;

    /// Constructores
    /// Constructor vacío-> necesario para JPA para que pueda crear el objeto la base de datos

    public Tienda() {
    }

    /// Constructor con parámetros-> Todos los necesarios de una Tienda
    public Tienda(Long id, String nombre, String url) {
        this.id = id;
        this.nombre = nombre;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
