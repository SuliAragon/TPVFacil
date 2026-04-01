package com.tpvfacil.verifactu;

/**
 * Encapsula la respuesta recibida de la API AEAT tras enviar una factura Verifactu.
 */
public class RespuestaAeat {

    private boolean correcto;
    private String csv;             // Código de verificación si correcto
    private String codigoError;     // Código de error si incorrecto
    private String descripcionError;
    private String xmlRespuesta;    // XML SOAP completo de la respuesta

    public RespuestaAeat() {}

    /** Crea una respuesta de éxito con su CSV. */
    public static RespuestaAeat exito(String csv) {
        RespuestaAeat r = new RespuestaAeat();
        r.correcto = true;
        r.csv = csv;
        return r;
    }

    /** Crea una respuesta de error con su código y descripción. */
    public static RespuestaAeat error(String codigoError, String descripcion) {
        RespuestaAeat r = new RespuestaAeat();
        r.correcto = false;
        r.codigoError = codigoError;
        r.descripcionError = descripcion;
        return r;
    }

    // --- Getters y Setters ---

    public boolean isCorrecto() { return correcto; }
    public void setCorrecto(boolean correcto) { this.correcto = correcto; }

    public String getCsv() { return csv; }
    public void setCsv(String csv) { this.csv = csv; }

    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }

    public String getDescripcionError() { return descripcionError; }
    public void setDescripcionError(String descripcionError) { this.descripcionError = descripcionError; }

    public String getXmlRespuesta() { return xmlRespuesta; }
    public void setXmlRespuesta(String xmlRespuesta) { this.xmlRespuesta = xmlRespuesta; }

    /** Indica si es un duplicado (ya enviada antes). */
    public boolean esDuplicada() {
        return "1300".equals(codigoError);
    }

    @Override
    public String toString() {
        if (correcto) return "RespuestaAeat{OK, csv='" + csv + "'}";
        return "RespuestaAeat{ERROR, codigo='" + codigoError + "', msg='" + descripcionError + "'}";
    }
}
