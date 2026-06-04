package com.david.ProyectoFinal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrapings_pendientes")
public class ScrapingPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoScrapingPendiente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoScrapingPendiente estado;

    @Column(nullable = false)
    private Integer intentos;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String ultimoError;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltimoIntento;

    private LocalDateTime fechaProcesado;

    public ScrapingPendiente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoScrapingPendiente getTipo() {
        return tipo;
    }

    public void setTipo(TipoScrapingPendiente tipo) {
        this.tipo = tipo;
    }

    public EstadoScrapingPendiente getEstado() {
        return estado;
    }

    public void setEstado(EstadoScrapingPendiente estado) {
        this.estado = estado;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimoIntento() {
        return fechaUltimoIntento;
    }

    public void setFechaUltimoIntento(LocalDateTime fechaUltimoIntento) {
        this.fechaUltimoIntento = fechaUltimoIntento;
    }

    public LocalDateTime getFechaProcesado() {
        return fechaProcesado;
    }

    public void setFechaProcesado(LocalDateTime fechaProcesado) {
        this.fechaProcesado = fechaProcesado;
    }
}
