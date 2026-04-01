package com.tpvfacil.core.modelo;

/**
 * Tipos de negocio soportados por el TPV.
 */
public enum TipoNegocio {

    HOSTELERIA("Hostelería", "#E67E22"),
    COMERCIO("Comercio", "#1B4F8A");

    private final String descripcion;
    private final String color;

    TipoNegocio(String descripcion, String color) {
        this.descripcion = descripcion;
        this.color = color;
    }

    /** Descripción en español para mostrar en la interfaz. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Color hex asociado a este tipo de negocio. */
    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
