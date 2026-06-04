package com.david.ProyectoFinal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Talla {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    TALLA_35("35"),
    TALLA_36("36"),
    TALLA_37("37"),
    TALLA_38("38"),
    TALLA_39("39"),
    TALLA_40("40"),
    TALLA_41("41"),
    TALLA_42("42"),
    UNICA("Unica");

    private final String etiqueta;

    Talla(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    @JsonCreator
    public static Talla fromJson(String valor) {
        if (valor == null) {
            return null;
        }

        String valorLimpio = valor.trim();
        if (valorLimpio.isEmpty()) {
            return null;
        }

        for (Talla talla : values()) {
            if (talla.name().equalsIgnoreCase(valorLimpio)
                    || talla.etiqueta.equalsIgnoreCase(valorLimpio)) {
                return talla;
            }
        }

        String valorMayusculas = valorLimpio.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        if (valorMayusculas.matches("^\\d+$")) {
            valorMayusculas = "TALLA_" + valorMayusculas;
        }

        if ("TALLA_UNICA".equals(valorMayusculas)) {
            valorMayusculas = "UNICA";
        }

        for (Talla talla : values()) {
            if (talla.name().equals(valorMayusculas)) {
                return talla;
            }
        }

        throw new IllegalArgumentException("Talla no valida: " + valor);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
