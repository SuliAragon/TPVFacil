package com.tpvfacil.verifactu;

import java.math.BigDecimal;

/**
 * Representa el registro de una factura en el sistema Verifactu.
 * Contiene los datos calculados (hash, firma) y la respuesta de la AEAT.
 */
public class RegistroFactura {

    private int facturaId;
    private String idEmisorFactura;      // NIF del negocio
    private String numSerieFactura;      // "A-000123"
    private String fechaExpedicion;      // "dd-MM-yyyy"
    private String tipoFactura;          // "F1" (completa) o "F2" (ticket/simplificada)
    private BigDecimal baseImponible;
    private BigDecimal cuotaIva;
    private BigDecimal importeTotal;
    private String huella;              // SHA-256 calculado (64 chars hex)
    private String huellaAnterior;      // Hash de la factura anterior (64 ceros si es la primera)
    private String firma;               // Firma digital en Base64
    private String xmlEnviado;          // XML SOAP enviado a la AEAT
    private String csvAeat;             // Código de verificación devuelto por la AEAT
    private EstadoEnvio estado;
    private String errorDescripcion;
    private int intentos;

    public RegistroFactura() {
        this.estado = EstadoEnvio.PENDIENTE;
        this.intentos = 0;
    }

    // --- Getters y Setters ---

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public String getIdEmisorFactura() { return idEmisorFactura; }
    public void setIdEmisorFactura(String idEmisorFactura) { this.idEmisorFactura = idEmisorFactura; }

    public String getNumSerieFactura() { return numSerieFactura; }
    public void setNumSerieFactura(String numSerieFactura) { this.numSerieFactura = numSerieFactura; }

    public String getFechaExpedicion() { return fechaExpedicion; }
    public void setFechaExpedicion(String fechaExpedicion) { this.fechaExpedicion = fechaExpedicion; }

    public String getTipoFactura() { return tipoFactura; }
    public void setTipoFactura(String tipoFactura) { this.tipoFactura = tipoFactura; }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }

    public String getHuella() { return huella; }
    public void setHuella(String huella) { this.huella = huella; }

    public String getHuellaAnterior() { return huellaAnterior; }
    public void setHuellaAnterior(String huellaAnterior) { this.huellaAnterior = huellaAnterior; }

    public String getFirma() { return firma; }
    public void setFirma(String firma) { this.firma = firma; }

    public String getXmlEnviado() { return xmlEnviado; }
    public void setXmlEnviado(String xmlEnviado) { this.xmlEnviado = xmlEnviado; }

    public String getCsvAeat() { return csvAeat; }
    public void setCsvAeat(String csvAeat) { this.csvAeat = csvAeat; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public String getErrorDescripcion() { return errorDescripcion; }
    public void setErrorDescripcion(String errorDescripcion) { this.errorDescripcion = errorDescripcion; }

    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    /** Incrementa el contador de intentos de envío. */
    public void incrementarIntentos() { this.intentos++; }

    @Override
    public String toString() {
        return "RegistroFactura{facturaId=" + facturaId
                + ", numSerie='" + numSerieFactura + "'"
                + ", estado=" + estado + "}";
    }
}
