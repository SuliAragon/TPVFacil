package com.tpvfacil.verifactu.excepciones;

/**
 * Excepción para errores de comunicación o respuesta de la API de la AEAT.
 * Incluye el código de error devuelto por la AEAT si está disponible.
 */
public class AeatException extends VerifactuException {

    private final String codigoError;

    public AeatException(String mensaje) {
        super(mensaje);
        this.codigoError = null;
    }

    public AeatException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public AeatException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = null;
    }

    /** Código de error de la AEAT (ej: "1105", "1200"). Puede ser null. */
    public String getCodigoError() {
        return codigoError;
    }

    /** Indica si la factura ya fue registrada anteriormente en la AEAT (código 1300). */
    public boolean esDuplicada() {
        return "1300".equals(codigoError);
    }
}
