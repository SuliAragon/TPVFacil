package com.tpvfacil.licencia;

import com.tpvfacil.config.ConfiguracionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.UUID;

/**
 * Genera un identificador único del equipo para vincular la licencia al hardware.
 * Combina la dirección MAC de la primera interfaz no-loopback con el hostname.
 * Resultado: siempre exactamente 16 caracteres en mayúsculas.
 */
public class HardwareFingerprint {

    private static final Logger log = LoggerFactory.getLogger(HardwareFingerprint.class);

    private HardwareFingerprint() {}

    /**
     * Genera el fingerprint del hardware actual.
     * Si no puede obtener la MAC, usa un UUID almacenado en ConfiguracionManager.
     *
     * @return String de exactamente 16 caracteres en mayúsculas
     */
    public static String generar() {
        try {
            String mac = obtenerMac();
            String hostname = obtenerHostname();
            String input = mac + "|" + hostname;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02X", b));
            }

            return hex.substring(0, 16);

        } catch (Exception e) {
            log.warn("No se pudo calcular fingerprint de hardware, usando UUID de respaldo", e);
            return obtenerFallback();
        }
    }

    private static String obtenerMac() throws Exception {
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isLoopback() || !ni.isUp()) continue;
            byte[] mac = ni.getHardwareAddress();
            if (mac == null || mac.length == 0) continue;

            StringBuilder sb = new StringBuilder();
            for (byte b : mac) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        throw new RuntimeException("No se encontró ninguna interfaz de red activa");
    }

    private static String obtenerHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Fallback: UUID aleatorio que persiste en ConfiguracionManager.
     * Sirve para equipos sin interfaz de red.
     */
    private static String obtenerFallback() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String fallback = cfg.get(ConfiguracionManager.HW_FINGERPRINT_FALLBACK);
        if (fallback != null && fallback.length() == 16) {
            return fallback;
        }
        // Generar nuevo UUID de respaldo
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16);
        cfg.set(ConfiguracionManager.HW_FINGERPRINT_FALLBACK, uuid);
        return uuid;
    }
}
