package com.tpvfacil.core.modelo;

/**
 * Formas de pago disponibles en el TPV.
 */
public enum FormaPago {

    EFECTIVO("Efectivo", "#27AE60"),
    TARJETA("Tarjeta", "#2980B9"),
    MIXTO("Mixto", "#8E44AD");

    private final String descripcion;
    private final String color;

    FormaPago(String descripcion, String color) {
        this.descripcion = descripcion;
        this.color = color;
    }

    /** Descripción en español para mostrar en la interfaz. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Color hex asociado a esta forma de pago. */
    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
