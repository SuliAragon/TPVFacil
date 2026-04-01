package com.tpvfacil.verifactu.excepciones;

/**
 * Excepción base para errores en el proceso Verifactu.
 * No debe capturarse en silencio — siempre registrar o relanzar.
 */
public class VerifactuException extends Exception {

    public VerifactuException(String mensaje) {
        super(mensaje);
    }

    public VerifactuException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
