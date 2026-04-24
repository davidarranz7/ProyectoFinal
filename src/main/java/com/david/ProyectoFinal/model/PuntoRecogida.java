package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "puntos_recogida")
public class PuntoRecogida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private String ciudad;
    private String provincia;

    private Boolean disponible;
    private String motivoNoDisponible;

    public PuntoRecogida() {
    }

    public PuntoRecogida(Long id, String nombre, String direccion, String ciudad, String provincia, Boolean disponible, String motivoNoDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.disponible = disponible;
        this.motivoNoDisponible = motivoNoDisponible;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public String getMotivoNoDisponible() {
        return motivoNoDisponible;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public void setMotivoNoDisponible(String motivoNoDisponible) {
        this.motivoNoDisponible = motivoNoDisponible;
    }
}