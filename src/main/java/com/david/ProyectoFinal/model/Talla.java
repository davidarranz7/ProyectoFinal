package com.david.ProyectoFinal.model;

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

    @Override
    public String toString() {
        return etiqueta;
    }
}
