package com.tpvfacil.hosteleria.modelo;

/**
 * Estados posibles de una mesa del restaurante/bar.
 */
public enum EstadoMesa {

    LIBRE("Libre", "#27AE60"),
    OCUPADA("Ocupada", "#E74C3C"),
    PENDIENTE_PAGO("Pendiente de pago", "#F39C12");

    private final String descripcion;
    private final String color;

    EstadoMesa(String descripcion, String color) {
        this.descripcion = descripcion;
        this.color = color;
    }

    /** Descripción en español para mostrar en la tarjeta de mesa. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Color hex para el indicador visual de estado. */
    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
