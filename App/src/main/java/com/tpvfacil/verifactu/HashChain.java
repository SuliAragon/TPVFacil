package com.tpvfacil.verifactu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calcula el hash SHA-256 encadenado entre facturas para Verifactu.
 *
 * Algoritmo oficial AEAT:
 * Concatenar: IDEmisor|NumSerie|Fecha|Tipo|CuotaTotal|ImporteTotal|HashAnterior
 * Aplicar SHA-256 → hexadecimal uppercase (64 caracteres)
 *
 * Primera factura de la serie: hashAnterior = 64 ceros ("000...000")
 */
public class HashChain {

    /** Hash inicial para la primera factura de la cadena (64 ceros). */
    public static final String HASH_INICIAL = "0".repeat(64);

    private static final String SEPARADOR = "|";

    private HashChain() {}

    /**
     * Calcula el hash de una factura encadenado con el hash de la anterior.
     *
     * @param registro     Datos de la factura a registrar
     * @param hashAnterior Hash de la factura anterior (HASH_INICIAL si es la primera)
     * @return Hash SHA-256 en hexadecimal uppercase (64 caracteres)
     */
    public static String calcularHash(RegistroFactura registro, String hashAnterior) {
        String contenido = construirCadena(registro, hashAnterior);
        return sha256Hex(contenido);
    }

    /**
     * Construye la cadena a hashear según el formato oficial AEAT.
     * IDEmisor|NumSerie|Fecha|Tipo|CuotaTotal|ImporteTotal|HashAnterior
     */
    static String construirCadena(RegistroFactura registro, String hashAnterior) {
        return registro.getIdEmisorFactura() + SEPARADOR
                + registro.getNumSerieFactura() + SEPARADOR
                + registro.getFechaExpedicion() + SEPARADOR
                + registro.getTipoFactura() + SEPARADOR
                + formatearImporte(registro.getCuotaIva()) + SEPARADOR
                + formatearImporte(registro.getImporteTotal()) + SEPARADOR
                + hashAnterior;
    }

    /**
     * Aplica SHA-256 a un texto y devuelve el resultado en hexadecimal uppercase.
     *
     * @param texto Texto a hashear (UTF-8)
     * @return String de 64 caracteres hexadecimales en mayúsculas
     */
    public static String sha256Hex(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre está disponible en Java
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /**
     * Formatea un BigDecimal con 2 decimales y punto como separador decimal.
     * Ejemplo: 7.30, 0.44, 100.00
     */
    private static String formatearImporte(java.math.BigDecimal importe) {
        if (importe == null) return "0.00";
        return String.format("%.2f", importe).replace(",", ".");
    }
}
