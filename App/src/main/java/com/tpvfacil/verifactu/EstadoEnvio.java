package com.tpvfacil.verifactu;

/**
 * Estado del registro de una factura en el sistema Verifactu (AEAT).
 */
public enum EstadoEnvio {

    /** Enviado y aceptado por la AEAT. Tiene CSV. */
    OK("Correcto", "#27AE60"),

    /** Pendiente de enviar (sin conexión o error temporal). */
    PENDIENTE("Pendiente", "#F39C12"),

    /** Error persistente tras reintentos. Requiere revisión manual. */
    ERROR("Error", "#E74C3C");

    private final String descripcion;
    private final String color;

    EstadoEnvio(String descripcion, String color) {
        this.descripcion = descripcion;
        this.color = color;
    }

    public String getDescripcion() { return descripcion; }
    public String getColor() { return color; }

    @Override
    public String toString() { return descripcion; }
}
