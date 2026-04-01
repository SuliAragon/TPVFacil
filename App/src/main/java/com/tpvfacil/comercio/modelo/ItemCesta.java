package com.tpvfacil.comercio.modelo;

import com.tpvfacil.core.modelo.Producto;

/**
 * Representa un ítem en la cesta de la compra del TPV de comercio.
 * El subtotal se calcula automáticamente a partir del producto y la cantidad.
 */
public class ItemCesta {

    private Producto producto;
    private double cantidad;

    /** Constructor con producto y cantidad. */
    public ItemCesta(Producto producto, double cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    /** Subtotal calculado: precio * cantidad (sin IVA). */
    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    /** Subtotal con IVA incluido. */
    public double getSubtotalConIva() {
        return producto.getPrecioConIva() * cantidad;
    }

    /** Incrementa la cantidad en 1. */
    public void incrementar() {
        this.cantidad++;
    }

    /** Decrementa la cantidad en 1 (no baja de 1). */
    public void decrementar() {
        if (this.cantidad > 1) {
            this.cantidad--;
        }
    }

    @Override
    public String toString() {
        return "ItemCesta{producto=" + producto.getNombre() + ", cantidad=" + cantidad + ", subtotal=" + getSubtotal() + "}";
    }
}
