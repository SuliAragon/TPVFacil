package com.tpvfacil.hosteleria.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa un pedido abierto en una mesa del establecimiento.
 * Una comanda puede tener múltiples líneas con los productos pedidos.
 */
public class Comanda {

    /** Estados del ciclo de vida de una comanda. */
    public enum Estado {
        ABIERTA("Abierta"),
        CERRADA("Cerrada"),
        PAGADA("Pagada");

        private final String descripcion;

        Estado(String descripcion) { this.descripcion = descripcion; }

        public String getDescripcion() { return descripcion; }

        @Override
        public String toString() { return descripcion; }
    }

    private int id;
    private int mesaId;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private int numComensales;
    private Estado estado;
    private int facturaId;      // 0 = sin factura aún
    private String observaciones;
    private List<LineaComanda> lineas;

    /** Constructor vacío requerido para los Repositories. */
    public Comanda() {
        this.numComensales = 1;
        this.estado = Estado.ABIERTA;
        this.lineas = new ArrayList<>();
    }

    /** Constructor completo. */
    public Comanda(int id, int mesaId, LocalDateTime fechaApertura, LocalDateTime fechaCierre,
                   int numComensales, Estado estado, int facturaId, String observaciones) {
        this.id = id;
        this.mesaId = mesaId;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
        this.numComensales = numComensales;
        this.estado = estado;
        this.facturaId = facturaId;
        this.observaciones = observaciones;
        this.lineas = new ArrayList<>();
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMesaId() { return mesaId; }
    public void setMesaId(int mesaId) { this.mesaId = mesaId; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public int getNumComensales() { return numComensales; }
    public void setNumComensales(int numComensales) { this.numComensales = numComensales; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<LineaComanda> getLineas() { return lineas; }
    public void setLineas(List<LineaComanda> lineas) { this.lineas = lineas; }

    /** Calcula el total de la comanda sumando todas las líneas. */
    public double getTotal() {
        return lineas.stream().mapToDouble(LineaComanda::getSubtotal).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comanda)) return false;
        Comanda c = (Comanda) o;
        return id == c.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comanda{id=" + id + ", mesaId=" + mesaId + ", estado=" + estado + "}";
    }
}
