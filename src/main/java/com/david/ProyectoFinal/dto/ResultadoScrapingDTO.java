package com.david.ProyectoFinal.dto;

import com.david.ProyectoFinal.model.TipoCambioPrecio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResultadoScrapingDTO {

    private String nombreProceso;
    private int totalProductosEncontrados;
    private int totalProductosGuardados;
    private int totalProductosNuevos;
    private int totalProductosActualizados;
    private int totalProductosDesactivados;
    private int totalProductosCambioPrecio;
    private int totalProductosBajadaPrecio;
    private int totalProductosSubidaPrecio;
    private int totalProductosRebajaMayor;
    private int totalProductosSinImagen;
    private int totalProductosSinPrecio;
    private long duracionMs;
    private boolean pendiente;
    private String mensajeEstado;
    private List<CambioPrecioProductoDTO> cambiosPrecio = new ArrayList<>();
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

    public int getTotalProductosDesactivados() {
        return totalProductosDesactivados;
    }

    public void setTotalProductosDesactivados(int totalProductosDesactivados) {
        this.totalProductosDesactivados = totalProductosDesactivados;
    }

    public int getTotalProductosSinImagen() {
        return totalProductosSinImagen;
    }

    public int getTotalProductosCambioPrecio() {
        return totalProductosCambioPrecio;
    }

    public void setTotalProductosCambioPrecio(int totalProductosCambioPrecio) {
        this.totalProductosCambioPrecio = totalProductosCambioPrecio;
    }

    public int getTotalProductosBajadaPrecio() {
        return totalProductosBajadaPrecio;
    }

    public void setTotalProductosBajadaPrecio(int totalProductosBajadaPrecio) {
        this.totalProductosBajadaPrecio = totalProductosBajadaPrecio;
    }

    public int getTotalProductosSubidaPrecio() {
        return totalProductosSubidaPrecio;
    }

    public void setTotalProductosSubidaPrecio(int totalProductosSubidaPrecio) {
        this.totalProductosSubidaPrecio = totalProductosSubidaPrecio;
    }

    public int getTotalProductosRebajaMayor() {
        return totalProductosRebajaMayor;
    }

    public void setTotalProductosRebajaMayor(int totalProductosRebajaMayor) {
        this.totalProductosRebajaMayor = totalProductosRebajaMayor;
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

    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public List<CambioPrecioProductoDTO> getCambiosPrecio() {
        return cambiosPrecio;
    }

    public void setCambiosPrecio(List<CambioPrecioProductoDTO> cambiosPrecio) {
        this.cambiosPrecio = cambiosPrecio == null ? new ArrayList<>() : cambiosPrecio;
    }

    public void setMensajeEstado(String mensajeEstado) {
        this.mensajeEstado = mensajeEstado;
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

    public void sumarProductoDesactivado() {
        this.totalProductosDesactivados++;
    }

    public void registrarCambioPrecio(CambioPrecioProductoDTO cambioPrecio) {
        if (cambioPrecio == null) {
            return;
        }

        this.totalProductosCambioPrecio++;

        if (cambioPrecio.getTipoCambio() == TipoCambioPrecio.BAJADA) {
            this.totalProductosBajadaPrecio++;
        } else if (cambioPrecio.getTipoCambio() == TipoCambioPrecio.SUBIDA) {
            this.totalProductosSubidaPrecio++;
        }

        if (Boolean.TRUE.equals(cambioPrecio.getRebajaMayor())) {
            this.totalProductosRebajaMayor++;
        }

        this.cambiosPrecio.add(cambioPrecio);
    }

    public void ordenarYLimitarCambiosPrecio(int limite) {
        if (this.cambiosPrecio == null || this.cambiosPrecio.isEmpty() || limite <= 0) {
            return;
        }

        this.cambiosPrecio = this.cambiosPrecio.stream()
                .sorted(
                        Comparator.comparing(this::prioridadCambio)
                                .thenComparing(
                                        cambio -> cambio.getPorcentajeVariacionPrecio() == null
                                                ? java.math.BigDecimal.ZERO
                                                : cambio.getPorcentajeVariacionPrecio().abs(),
                                        Comparator.reverseOrder()
                                )
                )
                .limit(limite)
                .toList();
    }

    public void sumarProductoSinImagen() {
        this.totalProductosSinImagen++;
    }

    public void sumarProductoSinPrecio() {
        this.totalProductosSinPrecio++;
    }

    private int prioridadCambio(CambioPrecioProductoDTO cambio) {
        if (cambio == null || cambio.getTipoCambio() == null) {
            return 3;
        }

        if (cambio.getTipoCambio() == TipoCambioPrecio.BAJADA) {
            return Boolean.TRUE.equals(cambio.getRebajaMayor()) ? 0 : 1;
        }

        if (cambio.getTipoCambio() == TipoCambioPrecio.SUBIDA) {
            return 2;
        }

        return 3;
    }
}
