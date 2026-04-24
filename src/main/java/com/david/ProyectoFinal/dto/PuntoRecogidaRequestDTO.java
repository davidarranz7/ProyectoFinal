package com.david.ProyectoFinal.dto;

public class PuntoRecogidaRequestDTO {

    private String nombre;
    private String direccion;
    private String ciudad;
    private String provincia;
    private Boolean disponible;
    private String motivoNoDisponible;

    public PuntoRecogidaRequestDTO() {
    }

    public PuntoRecogidaRequestDTO(String nombre, String direccion, String ciudad, String provincia, Boolean disponible, String motivoNoDisponible) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.disponible = disponible;
        this.motivoNoDisponible = motivoNoDisponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getMotivoNoDisponible() {
        return motivoNoDisponible;
    }

    public void setMotivoNoDisponible(String motivoNoDisponible) {
        this.motivoNoDisponible = motivoNoDisponible;
    }
}