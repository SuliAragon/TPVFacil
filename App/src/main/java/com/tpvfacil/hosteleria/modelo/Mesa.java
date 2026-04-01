package com.tpvfacil.hosteleria.modelo;

import java.util.Objects;

/**
 * Representa una mesa física del establecimiento de hostelería.
 */
public class Mesa {

    private int id;
    private String nombre;
    private int capacidad;
    private String zona;
    private EstadoMesa estado;
    private boolean activa;

    /** Constructor vacío requerido para los Repositories. */
    public Mesa() {
        this.capacidad = 4;
        this.zona = "Sala";
        this.estado = EstadoMesa.LIBRE;
        this.activa = true;
    }

    /** Constructor completo. */
    public Mesa(int id, String nombre, int capacidad, String zona, EstadoMesa estado, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.zona = zona;
        this.estado = estado;
        this.activa = activa;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public EstadoMesa getEstado() { return estado; }
    public void setEstado(EstadoMesa estado) { this.estado = estado; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mesa)) return false;
        Mesa m = (Mesa) o;
        return id == m.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Mesa{id=" + id + ", nombre='" + nombre + "', estado=" + estado + "}";
    }
}
