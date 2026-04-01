package com.tpvfacil.core.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una factura emitida por el negocio.
 * Cada factura está vinculada al sistema Verifactu (AEAT) a través de registros_verifactu.
 */
public class Factura {

    private int id;
    private String serie;
    private int numero;
    private LocalDateTime fecha;
    private int clienteId;      // 0 = sin cliente asociado
    private List<LineaFactura> lineas;
    private double baseImponible;
    private double cuotaIva;
    private double total;
    private FormaPago formaPago;
    private double efectivoEntregado;
    private double cambio;
    private TipoNegocio tipoNegocio;
    private boolean anulada;

    /** Constructor vacío requerido para los Repositories. */
    public Factura() {
        this.serie = "A";
        this.lineas = new ArrayList<>();
        this.anulada = false;
    }

    /** Constructor completo. */
    public Factura(int id, String serie, int numero, LocalDateTime fecha, int clienteId,
                   double baseImponible, double cuotaIva, double total,
                   FormaPago formaPago, double efectivoEntregado, double cambio,
                   TipoNegocio tipoNegocio, boolean anulada) {
        this.id = id;
        this.serie = serie;
        this.numero = numero;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.baseImponible = baseImponible;
        this.cuotaIva = cuotaIva;
        this.total = total;
        this.formaPago = formaPago;
        this.efectivoEntregado = efectivoEntregado;
        this.cambio = cambio;
        this.tipoNegocio = tipoNegocio;
        this.anulada = anulada;
        this.lineas = new ArrayList<>();
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public List<LineaFactura> getLineas() { return lineas; }
    public void setLineas(List<LineaFactura> lineas) { this.lineas = lineas; }

    public double getBaseImponible() { return baseImponible; }
    public void setBaseImponible(double baseImponible) { this.baseImponible = baseImponible; }

    public double getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(double cuotaIva) { this.cuotaIva = cuotaIva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }

    public double getEfectivoEntregado() { return efectivoEntregado; }
    public void setEfectivoEntregado(double efectivoEntregado) { this.efectivoEntregado = efectivoEntregado; }

    public double getCambio() { return cambio; }
    public void setCambio(double cambio) { this.cambio = cambio; }

    public TipoNegocio getTipoNegocio() { return tipoNegocio; }
    public void setTipoNegocio(TipoNegocio tipoNegocio) { this.tipoNegocio = tipoNegocio; }

    public boolean isAnulada() { return anulada; }
    public void setAnulada(boolean anulada) { this.anulada = anulada; }

    /** Devuelve el número de factura con serie: ej. "A-0001". */
    public String getNumeroCompleto() {
        return serie + "-" + String.format("%04d", numero);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura f = (Factura) o;
        return id == f.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Factura{id=" + id + ", numero='" + getNumeroCompleto() + "', total=" + total + "}";
    }
}
