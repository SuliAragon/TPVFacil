package com.tpvfacil.verifactu;

import com.tpvfacil.licencia.HardwareFingerprint;
import com.tpvfacil.verifactu.excepciones.VerifactuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;

/**
 * Gestiona el certificado digital usado para firmar las facturas Verifactu.
 * La contraseña del certificado se cifra con AES-256 vinculada al hardware del equipo.
 * El KeyStore se cachea en memoria para no releer en cada factura.
 */
public class CertificadoManager {

    private static final Logger log = LoggerFactory.getLogger(CertificadoManager.class);
    private static final String SALT = "salt_tpvfacil";
    private static final String AES_ALGO = "AES/CBC/PKCS5Padding";

    private KeyStore keyStoreCacheado;
    private String aliasCacheado;

    /**
     * Carga el KeyStore desde un archivo .p12/.pfx.
     * El resultado queda cacheado en memoria.
     *
     * @param rutaP12  Ruta al archivo .p12 o .pfx
     * @param password Contraseña del certificado (en claro)
     * @return KeyStore cargado
     * @throws VerifactuException si el archivo no existe o la contraseña es incorrecta
     */
    public KeyStore cargarCertificado(Path rutaP12, String password) throws VerifactuException {
        try (FileInputStream fis = new FileInputStream(rutaP12.toFile())) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(fis, password.toCharArray());
            validarCertificado(ks);
            this.keyStoreCacheado = ks;
            this.aliasCacheado = obtenerAlias(ks);
            log.info("Certificado cargado: alias={}", aliasCacheado);
            return ks;
        } catch (VerifactuException e) {
            throw e;
        } catch (Exception e) {
            throw new VerifactuException("Error al cargar el certificado: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve el KeyStore cacheado o null si no se ha cargado aún.
     */
    public KeyStore getKeyStoreCacheado() {
        return keyStoreCacheado;
    }

    /**
     * Devuelve el alias del primer certificado encontrado en el KeyStore.
     */
    public String getAlias() {
        return aliasCacheado;
    }

    /**
     * Valida que el KeyStore contiene al menos un certificado con clave privada.
     *
     * @throws VerifactuException si el certificado no es válido o está expirado
     */
    public void validarCertificado(KeyStore ks) throws VerifactuException {
        try {
            String alias = obtenerAlias(ks);
            if (alias == null) {
                throw new VerifactuException("El archivo no contiene ningún certificado con clave privada.");
            }
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            cert.checkValidity();
        } catch (VerifactuException e) {
            throw e;
        } catch (Exception e) {
            throw new VerifactuException("Certificado no válido: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la fecha de expiración del certificado.
     */
    public LocalDate fechaExpiracion(KeyStore ks) throws VerifactuException {
        try {
            String alias = obtenerAlias(ks);
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            Date expiracion = cert.getNotAfter();
            return expiracion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            throw new VerifactuException("No se pudo obtener la fecha de expiración", e);
        }
    }

    /**
     * Indica si el certificado expira en menos de {@code dias} días.
     */
    public boolean estaProximoAVencer(KeyStore ks, int dias) throws VerifactuException {
        LocalDate expiracion = fechaExpiracion(ks);
        return LocalDate.now().plusDays(dias).isAfter(expiracion);
    }

    /**
     * Cifra una contraseña con AES-256 vinculada al hardware del equipo.
     * La clave AES se deriva del hardware fingerprint.
     *
     * @param passwordClaro Contraseña en texto plano
     * @return Contraseña cifrada en Base64
     */
    public String cifrarPassword(String passwordClaro) throws VerifactuException {
        try {
            byte[] claveAes = derivarClaveAes();
            SecretKeySpec keySpec = new SecretKeySpec(claveAes, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            // IV fijo derivado de la clave (para determinismo — el IV se incorpora implícitamente)
            byte[] iv = Arrays.copyOf(claveAes, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] cifrado = cipher.doFinal(passwordClaro.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            throw new VerifactuException("Error al cifrar la contraseña del certificado", e);
        }
    }

    /**
     * Descifra una contraseña cifrada con {@link #cifrarPassword}.
     *
     * @param passwordCifrada Contraseña en Base64
     * @return Contraseña en texto plano
     */
    public String descifrarPassword(String passwordCifrada) throws VerifactuException {
        try {
            byte[] claveAes = derivarClaveAes();
            SecretKeySpec keySpec = new SecretKeySpec(claveAes, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            byte[] iv = Arrays.copyOf(claveAes, 16);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] descifrado = cipher.doFinal(Base64.getDecoder().decode(passwordCifrada));
            return new String(descifrado, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new VerifactuException("Error al descifrar la contraseña del certificado", e);
        }
    }

    // --- Métodos privados ---

    private byte[] derivarClaveAes() throws Exception {
        String input = HardwareFingerprint.generar() + SALT;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private String obtenerAlias(KeyStore ks) throws Exception {
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (ks.isKeyEntry(alias)) return alias;
        }
        return null;
    }
}
