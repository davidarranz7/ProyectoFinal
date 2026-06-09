package com.david.ProyectoFinal.dto;

public class SuscripcionPushRequestDTO {

    private String endpoint;
    private Long expirationTime;
    private KeysDTO keys;

    public SuscripcionPushRequestDTO() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public KeysDTO getKeys() {
        return keys;
    }

    public void setKeys(KeysDTO keys) {
        this.keys = keys;
    }

    public static class KeysDTO {
        private String p256dh;
        private String auth;

        public KeysDTO() {
        }

        public String getP256dh() {
            return p256dh;
        }

        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }
    }
}
