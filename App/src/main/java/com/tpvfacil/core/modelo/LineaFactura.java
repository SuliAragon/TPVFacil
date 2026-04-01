package com.tpvfacil.core.modelo;

import java.util.Objects;

/**
 * Representa una línea de una factura (un producto vendido).
 * Guarda una instantánea (snapshot) de los datos del producto en el momento de la venta.
 */
public class LineaFactura {

    private int id;
    private int facturaId;
    private int productoId;
    private String descripcion;     // Snapshot del nombre del producto
    private double cantidad;
    private double precioUnitario;
    private double ivaPorcentaje;
    private double subtotal;

    /** Constructor vacío requerido para los Repositories. */
    public LineaFactura() {}

    /** Constructor completo. */
    public LineaFactura(int id, int facturaId, int productoId, String descripcion,
                        double cantidad, double precioUnitario, double ivaPorcentaje, double subtotal) {
        this.id = id;
        this.facturaId = facturaId;
        this.productoId = productoId;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.ivaPorcentaje = ivaPorcentaje;
        this.subtotal = subtotal;
    }

    /** Constructor conveniente para crear desde un Producto. */
    public LineaFactura(Producto producto, double cantidad) {
        this.productoId = producto.getId();
        this.descripcion = producto.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.ivaPorcentaje = producto.getIvaPorcentaje();
        this.subtotal = precioUnitario * cantidad;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(double ivaPorcentaje) { this.ivaPorcentaje = ivaPorcentaje; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    /** Calcula la cuota de IVA de esta línea. */
    public double getCuotaIva() {
        return subtotal * (ivaPorcentaje / 100.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaFactura)) return false;
        LineaFactura l = (LineaFactura) o;
        return id == l.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LineaFactura{id=" + id + ", descripcion='" + descripcion + "', cantidad=" + cantidad + ", subtotal=" + subtotal + "}";
    }
}
