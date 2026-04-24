package com.david.ProyectoFinal.dto;

public class ActualizarEstablecimientoDTO {

    private String nombre;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String nombreTienda;

    public ActualizarEstablecimientoDTO() {
    }

    public ActualizarEstablecimientoDTO(String nombre, String direccion, String ciudad, String provincia, String nombreTienda) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.nombreTienda = nombreTienda;
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

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }
}