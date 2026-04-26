package com.david.ProyectoFinal.dto;

public class RecuperarPasswordRequestDTO {

    private String identificador;

    public RecuperarPasswordRequestDTO() {
    }

    public RecuperarPasswordRequestDTO(String identificador) {
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}