package com.david.ProyectoFinal.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TallaConverter implements AttributeConverter<Talla, String> {

    @Override
    public String convertToDatabaseColumn(Talla talla) {
        return talla == null ? null : talla.getEtiqueta();
    }

    @Override
    public Talla convertToEntityAttribute(String valorBd) {
        return Talla.fromJson(valorBd);
    }
}
