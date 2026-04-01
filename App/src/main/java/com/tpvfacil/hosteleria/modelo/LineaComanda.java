package com.tpvfacil.hosteleria.modelo;

import java.util.Objects;

/**
 * Representa un ítem dentro de una comanda (pedido de mesa).
 * Guarda snapshot del nombre del producto en el momento del pedido.
 */
public class LineaComanda {

    /** Estados del ítem durante el flujo de cocina. */
    public enum Estado {
        PENDIENTE("Pendiente", "#F39C12"),
        EN_COCINA("En cocina", "#E67E22"),
        SERVIDO("Servido", "#27AE60");

        private final String descripcion;
        private final String color;

        Estado(String descripcion, String color) {
            this.descripcion = descripcion;
            this.color = color;
        }

        public String getDescripcion() { return descripcion; }
        public String getColor() { return color; }

        @Override
        public String toString() { return descripcion; }
    }

    private int id;
    private int comandaId;
    private int productoId;
    private String nombreProducto;  // Snapshot del nombre
    private double cantidad;
    private double precioUnitario;
    private boolean enviadoCocina;
    private Estado estado;
    private String observaciones;

    /** Constructor vacío requerido para los Repositories. */
    public LineaComanda() {
        this.cantidad = 1;
        this.enviadoCocina = false;
        this.estado = Estado.PENDIENTE;
    }

    /** Constructor completo. */
    public LineaComanda(int id, int comandaId, int productoId, String nombreProducto,
                        double cantidad, double precioUnitario,
                        boolean enviadoCocina, Estado estado, String observaciones) {
        this.id = id;
        this.comandaId = comandaId;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.enviadoCocina = enviadoCocina;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComandaId() { return comandaId; }
    public void setComandaId(int comandaId) { this.comandaId = comandaId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public boolean isEnviadoCocina() { return enviadoCocina; }
    public void setEnviadoCocina(boolean enviadoCocina) { this.enviadoCocina = enviadoCocina; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    /** Calcula el subtotal de esta línea (sin IVA). */
    public double getSubtotal() {
        return precioUnitario * cantidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaComanda)) return false;
        LineaComanda l = (LineaComanda) o;
        return id == l.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LineaComanda{id=" + id + ", producto='" + nombreProducto + "', cantidad=" + cantidad + "}";
    }
}
