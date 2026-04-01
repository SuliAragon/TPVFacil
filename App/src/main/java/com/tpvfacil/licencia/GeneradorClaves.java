package com.tpvfacil.licencia;

/**
 * Herramienta interna para generar claves de licencia.
 * NO forma parte de la aplicación principal — tiene su propio main().
 *
 * Uso:
 *   java -cp tpvfacil-1.0.0-shaded.jar com.tpvfacil.licencia.GeneradorClaves A3B7C2D1E4F5G6H7
 *
 * Salida:
 *   TPVF-X3K9-M2PQ-7RBN-X4WZ
 */
public class GeneradorClaves {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: GeneradorClaves <hardware-fingerprint>");
            System.err.println("Ejemplo: GeneradorClaves A3B7C2D1E4F5G6H7");
            System.exit(1);
        }

        String fingerprint = args[0].trim().toUpperCase();

        if (fingerprint.length() != 16) {
            System.err.println("Error: El fingerprint debe tener exactamente 16 caracteres.");
            System.err.println("Recibido: '" + fingerprint + "' (" + fingerprint.length() + " chars)");
            System.exit(1);
        }

        try {
            String clave = LicenciaManager.generarClave(fingerprint);
            System.out.println("Fingerprint: " + fingerprint);
            System.out.println("Clave:       " + clave);
        } catch (Exception e) {
            System.err.println("Error al generar clave: " + e.getMessage());
            System.exit(1);
        }
    }
}
