package com.david.ProyectoFinal.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoScrapingDTO {

    private String nombreProceso;
    private int totalProductosEncontrados;
    private int totalProductosGuardados;
    private int totalProductosNuevos;
    private int totalProductosActualizados;
    private int totalProductosSinImagen;
    private int totalProductosSinPrecio;
    private long duracionMs;
    private List<ResultadoScrapingTiendaDTO> resultadosPorTienda = new ArrayList<>();

    public ResultadoScrapingDTO() {
    }

    public ResultadoScrapingDTO(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setNombreProceso(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public int getTotalProductosEncontrados() {
        return totalProductosEncontrados;
    }

    public void setTotalProductosEncontrados(int totalProductosEncontrados) {
        this.totalProductosEncontrados = totalProductosEncontrados;
    }

    public int getTotalProductosGuardados() {
        return totalProductosGuardados;
    }

    public void setTotalProductosGuardados(int totalProductosGuardados) {
        this.totalProductosGuardados = totalProductosGuardados;
    }

    public int getTotalProductosNuevos() {
        return totalProductosNuevos;
    }

    public void setTotalProductosNuevos(int totalProductosNuevos) {
        this.totalProductosNuevos = totalProductosNuevos;
    }

    public int getTotalProductosActualizados() {
        return totalProductosActualizados;
    }

    public void setTotalProductosActualizados(int totalProductosActualizados) {
        this.totalProductosActualizados = totalProductosActualizados;
    }

    public int getTotalProductosSinImagen() {
        return totalProductosSinImagen;
    }

    public void setTotalProductosSinImagen(int totalProductosSinImagen) {
        this.totalProductosSinImagen = totalProductosSinImagen;
    }

    public int getTotalProductosSinPrecio() {
        return totalProductosSinPrecio;
    }

    public void setTotalProductosSinPrecio(int totalProductosSinPrecio) {
        this.totalProductosSinPrecio = totalProductosSinPrecio;
    }

    public long getDuracionMs() {
        return duracionMs;
    }

    public void setDuracionMs(long duracionMs) {
        this.duracionMs = duracionMs;
    }

    public List<ResultadoScrapingTiendaDTO> getResultadosPorTienda() {
        return resultadosPorTienda;
    }

    public void setResultadosPorTienda(List<ResultadoScrapingTiendaDTO> resultadosPorTienda) {
        this.resultadosPorTienda = resultadosPorTienda;
    }

    public void sumarProductoEncontrado() {
        this.totalProductosEncontrados++;
    }

    public void sumarProductoGuardado() {
        this.totalProductosGuardados++;
    }

    public void sumarProductoNuevo() {
        this.totalProductosNuevos++;
    }

    public void sumarProductoActualizado() {
        this.totalProductosActualizados++;
    }

    public void sumarProductoSinImagen() {
        this.totalProductosSinImagen++;
    }

    public void sumarProductoSinPrecio() {
        this.totalProductosSinPrecio++;
    }
}