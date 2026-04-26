package com.david.ProyectoFinal.dto;

public class CambiarPasswordRequestDTO {

    private String token;
    private String nuevaPassword;
    private String repetirPassword;

    public CambiarPasswordRequestDTO() {
    }

    public CambiarPasswordRequestDTO(String token, String nuevaPassword, String repetirPassword) {
        this.token = token;
        this.nuevaPassword = nuevaPassword;
        this.repetirPassword = repetirPassword;
    }

    public String getToken() {
        return token;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public String getRepetirPassword() {
        return repetirPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public void setRepetirPassword(String repetirPassword) {
        this.repetirPassword = repetirPassword;
    }
}