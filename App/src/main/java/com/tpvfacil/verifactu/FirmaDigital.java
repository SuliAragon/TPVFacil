package com.tpvfacil.verifactu;

import com.tpvfacil.verifactu.excepciones.VerifactuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Firma digitalmente el contenido de una factura usando el certificado del negocio.
 * Algoritmo: SHA256withRSA (compatible con los certificados de la AEAT).
 */
public class FirmaDigital {

    private static final Logger log = LoggerFactory.getLogger(FirmaDigital.class);
    private static final String ALGORITMO = "SHA256withRSA";

    private FirmaDigital() {}

    /**
     * Firma un texto con la clave privada del certificado y devuelve la firma en Base64.
     *
     * @param contenido Texto a firmar (normalmente la cadena del hash Verifactu)
     * @param keyStore  KeyStore cargado con el certificado .p12/.pfx
     * @param alias     Alias del certificado dentro del KeyStore
     * @param password  Contraseña del certificado (en claro)
     * @return Firma digital en Base64
     * @throws VerifactuException si hay algún problema con el certificado o la firma
     */
    public static String firmar(String contenido, KeyStore keyStore, String alias, String password)
            throws VerifactuException {
        try {
            PrivateKey clavePrivada = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
            if (clavePrivada == null) {
                throw new VerifactuException("No se encontró la clave privada en el certificado (alias: " + alias + ")");
            }

            Signature signature = Signature.getInstance(ALGORITMO);
            signature.initSign(clavePrivada);
            signature.update(contenido.getBytes(StandardCharsets.UTF_8));
            byte[] firmaBytes = signature.sign();

            String firmaBase64 = Base64.getEncoder().encodeToString(firmaBytes);
            log.debug("Factura firmada correctamente. Longitud firma: {} bytes", firmaBytes.length);
            return firmaBase64;

        } catch (VerifactuException e) {
            throw e;
        } catch (Exception e) {
            throw new VerifactuException("Error al firmar la factura: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica que una firma es válida para un contenido dado.
     * Útil para testing y diagnóstico.
     *
     * @param contenido    Texto original
     * @param firmaBase64  Firma en Base64 a verificar
     * @param keyStore     KeyStore con el certificado público
     * @param alias        Alias del certificado
     * @return true si la firma es válida
     */
    public static boolean verificar(String contenido, String firmaBase64, KeyStore keyStore, String alias) {
        try {
            java.security.cert.Certificate cert = keyStore.getCertificate(alias);
            Signature signature = Signature.getInstance(ALGORITMO);
            signature.initVerify(cert.getPublicKey());
            signature.update(contenido.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(firmaBase64));
        } catch (Exception e) {
            log.error("Error al verificar firma", e);
            return false;
        }
    }
}
