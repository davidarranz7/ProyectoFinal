package com.david.ProyectoFinal.dto;

import java.util.ArrayList;
import java.util.List;

public class EstadoScrapingAdminDTO {

    private boolean relayHabilitado;
    private boolean relayDisponible;
    private String relayMensaje;
    private boolean puentePrincipalConfigurado;
    private boolean puenteFallbackConfigurado;
    private String ultimoPuenteUsado;
    private String mensajePuente;
    private boolean automaticoHabilitado;
    private long frecuenciaAutomaticaMs;
    private long intervaloReintentoMs;
    private boolean scrapingEnCurso;
    private int totalPendientes;
    private ScrapingEjecucionAdminDTO ultimaEjecucion;
    private ResultadoScrapingDTO ultimoResultado;
    private List<ScrapingPendienteAdminDTO> pendientes = new ArrayList<>();

    public EstadoScrapingAdminDTO() {
    }

    public boolean isRelayHabilitado() {
        return relayHabilitado;
    }

    public void setRelayHabilitado(boolean relayHabilitado) {
        this.relayHabilitado = relayHabilitado;
    }

    public boolean isRelayDisponible() {
        return relayDisponible;
    }

    public void setRelayDisponible(boolean relayDisponible) {
        this.relayDisponible = relayDisponible;
    }

    public String getRelayMensaje() {
        return relayMensaje;
    }

    public void setRelayMensaje(String relayMensaje) {
        this.relayMensaje = relayMensaje;
    }

    public boolean isPuentePrincipalConfigurado() {
        return puentePrincipalConfigurado;
    }

    public void setPuentePrincipalConfigurado(boolean puentePrincipalConfigurado) {
        this.puentePrincipalConfigurado = puentePrincipalConfigurado;
    }

    public boolean isPuenteFallbackConfigurado() {
        return puenteFallbackConfigurado;
    }

    public void setPuenteFallbackConfigurado(boolean puenteFallbackConfigurado) {
        this.puenteFallbackConfigurado = puenteFallbackConfigurado;
    }

    public String getUltimoPuenteUsado() {
        return ultimoPuenteUsado;
    }

    public void setUltimoPuenteUsado(String ultimoPuenteUsado) {
        this.ultimoPuenteUsado = ultimoPuenteUsado;
    }

    public String getMensajePuente() {
        return mensajePuente;
    }

    public void setMensajePuente(String mensajePuente) {
        this.mensajePuente = mensajePuente;
    }

    public boolean isAutomaticoHabilitado() {
        return automaticoHabilitado;
    }

    public void setAutomaticoHabilitado(boolean automaticoHabilitado) {
        this.automaticoHabilitado = automaticoHabilitado;
    }

    public long getFrecuenciaAutomaticaMs() {
        return frecuenciaAutomaticaMs;
    }

    public void setFrecuenciaAutomaticaMs(long frecuenciaAutomaticaMs) {
        this.frecuenciaAutomaticaMs = frecuenciaAutomaticaMs;
    }

    public long getIntervaloReintentoMs() {
        return intervaloReintentoMs;
    }

    public void setIntervaloReintentoMs(long intervaloReintentoMs) {
        this.intervaloReintentoMs = intervaloReintentoMs;
    }

    public boolean isScrapingEnCurso() {
        return scrapingEnCurso;
    }

    public void setScrapingEnCurso(boolean scrapingEnCurso) {
        this.scrapingEnCurso = scrapingEnCurso;
    }

    public int getTotalPendientes() {
        return totalPendientes;
    }

    public void setTotalPendientes(int totalPendientes) {
        this.totalPendientes = totalPendientes;
    }

    public ScrapingEjecucionAdminDTO getUltimaEjecucion() {
        return ultimaEjecucion;
    }

    public void setUltimaEjecucion(ScrapingEjecucionAdminDTO ultimaEjecucion) {
        this.ultimaEjecucion = ultimaEjecucion;
    }

    public ResultadoScrapingDTO getUltimoResultado() {
        return ultimoResultado;
    }

    public void setUltimoResultado(ResultadoScrapingDTO ultimoResultado) {
        this.ultimoResultado = ultimoResultado;
    }

    public List<ScrapingPendienteAdminDTO> getPendientes() {
        return pendientes;
    }

    public void setPendientes(List<ScrapingPendienteAdminDTO> pendientes) {
        this.pendientes = pendientes == null ? new ArrayList<>() : pendientes;
    }
}
