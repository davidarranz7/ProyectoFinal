package com.david.ProyectoFinal.dto;

public class RecuperarUsuarioRequestDTO {

    private String email;

    public RecuperarUsuarioRequestDTO() {
    }

    public RecuperarUsuarioRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}