package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class ScrapingRelayResponseDTO {

    private String tipo;
    private String nombreProceso;
    private List<Producto> productos = new ArrayList<>();

    public ScrapingRelayResponseDTO() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setNombreProceso(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos == null ? new ArrayList<>() : productos;
    }
}
