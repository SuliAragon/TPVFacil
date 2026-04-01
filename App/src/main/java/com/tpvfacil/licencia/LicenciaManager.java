package com.tpvfacil.licencia;

import com.tpvfacil.config.ConfiguracionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Gestiona la activación y validación de la licencia del software.
 *
 * Algoritmo: HMAC-SHA256(hardware_fingerprint, CLAVE_SECRETA) → truncado y formateado
 * como TPVF-XXXX-XXXX-XXXX-XXXX.
 *
 * IMPORTANTE: La clave secreta embebida debe mantenerse confidencial.
 * Cambiar CLAVE_SECRETA invalida todas las licencias existentes.
 */
public class LicenciaManager {

    private static final Logger log = LoggerFactory.getLogger(LicenciaManager.class);

    // Clave secreta HMAC — NO modificar en producción sin regenerar todas las licencias
    private static final String CLAVE_SECRETA = "TPVFacil-Secret-2024-AEAT-Verifactu";
    private static final String PREFIJO = "TPVF";

    private static boolean modoDemo = true;

    private LicenciaManager() {}

    /**
     * Comprueba si hay una licencia válida guardada para este equipo.
     * Actualiza el estado interno de modoDemo.
     */
    public static void comprobar() {
        String claveGuardada = ConfiguracionManager.getInstance().get(ConfiguracionManager.LICENCIA_ACTIVADA);
        if (claveGuardada != null && !claveGuardada.isBlank()) {
            String fingerprint = HardwareFingerprint.generar();
            if (validarClave(claveGuardada, fingerprint)) {
                modoDemo = false;
                log.info("Licencia válida encontrada. Modo completo activado.");
                return;
            } else {
                log.warn("Licencia guardada no válida para este equipo. Revirtiendo a modo demo.");
                ConfiguracionManager.getInstance().set(ConfiguracionManager.LICENCIA_ACTIVADA, "");
            }
        }
        modoDemo = true;
        log.info("Sin licencia. Modo demo activado.");
    }

    /** Indica si la aplicación está en modo demo (sin licencia válida). */
    public static boolean isModoDemo() {
        return modoDemo;
    }

    /**
     * Valida si una clave de licencia es correcta para el fingerprint dado.
     *
     * @param clave       Clave en formato TPVF-XXXX-XXXX-XXXX-XXXX
     * @param fingerprint Hardware fingerprint del equipo (16 chars)
     * @return true si la clave es válida
     */
    public static boolean validarClave(String clave, String fingerprint) {
        if (clave == null || fingerprint == null) return false;
        try {
            String claveEsperada = generarClave(fingerprint);
            return claveEsperada.equalsIgnoreCase(clave.trim());
        } catch (Exception e) {
            log.error("Error al validar clave de licencia", e);
            return false;
        }
    }

    /**
     * Activa la licencia si la clave es válida para el equipo actual.
     * Guarda la clave en ConfiguracionManager y cambia a modo completo.
     *
     * @param clave Clave introducida por el usuario
     * @return true si la activación fue exitosa
     */
    public static boolean activar(String clave) {
        String fingerprint = HardwareFingerprint.generar();
        if (validarClave(clave, fingerprint)) {
            ConfiguracionManager.getInstance().set(ConfiguracionManager.LICENCIA_ACTIVADA, clave.trim().toUpperCase());
            modoDemo = false;
            log.info("Licencia activada correctamente.");
            return true;
        }
        log.warn("Intento de activación con clave inválida.");
        return false;
    }

    /** Desactiva la licencia y vuelve al modo demo. */
    public static void desactivar() {
        ConfiguracionManager.getInstance().set(ConfiguracionManager.LICENCIA_ACTIVADA, "");
        modoDemo = true;
        log.info("Licencia desactivada.");
    }

    /**
     * Genera la clave de licencia válida para un fingerprint dado.
     * Usado internamente y por GeneradorClaves.
     *
     * @param fingerprint Hardware fingerprint del equipo objetivo (16 chars)
     * @return Clave en formato TPVF-XXXX-XXXX-XXXX-XXXX
     */
    public static String generarClave(String fingerprint) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(fingerprint.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02X", b));
        }

        // Tomar los primeros 16 chars y formatear como TPVF-XXXX-XXXX-XXXX-XXXX
        String raw = hex.substring(0, 16).toUpperCase();
        return PREFIJO + "-" + raw.substring(0, 4) + "-" + raw.substring(4, 8)
                + "-" + raw.substring(8, 12) + "-" + raw.substring(12, 16);
    }
}
