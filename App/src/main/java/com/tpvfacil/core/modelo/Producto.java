package com.tpvfacil.core.modelo;

import java.util.Objects;

/**
 * Representa un producto o artículo del catálogo del negocio.
 * El campo stock = -1 indica que no se controla el inventario.
 */
public class Producto {

    private int id;
    private String nombre;
    private double precio;
    private double ivaPorcentaje;
    private String categoria;
    private String codigoBarras;
    private int stock;      // -1 = sin control de stock
    private boolean activo;

    /** Constructor vacío requerido para los Repositories. */
    public Producto() {
        this.ivaPorcentaje = 21.0;
        this.stock = -1;
        this.activo = true;
    }

    /** Constructor completo. */
    public Producto(int id, String nombre, double precio, double ivaPorcentaje,
                    String categoria, String codigoBarras, int stock, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.ivaPorcentaje = ivaPorcentaje;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.stock = stock;
        this.activo = activo;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(double ivaPorcentaje) { this.ivaPorcentaje = ivaPorcentaje; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Indica si este producto tiene control de stock activo. */
    public boolean tieneControlStock() {
        return stock >= 0;
    }

    /** Calcula el precio con IVA incluido. */
    public double getPrecioConIva() {
        return precio * (1 + ivaPorcentaje / 100.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto p = (Producto) o;
        return id == p.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
    }
}
