package com.tpvfacil.verifactu;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.verifactu.excepciones.VerifactuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Genera el código QR que aparece en el ticket según el estándar Verifactu.
 * La URL del QR apunta al portal de verificación de la AEAT.
 *
 * Tamaño según ancho de papel:
 *   58mm → 100x100 px
 *   80mm → 150x150 px
 */
public class QrGenerator {

    private static final Logger log = LoggerFactory.getLogger(QrGenerator.class);

    private static final String URL_VERIFICACION = "https://www2.agenciatributaria.gob.es/es13/h/verifactu";
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private QrGenerator() {}

    /**
     * Genera el QR del ticket Verifactu como imagen.
     *
     * @param factura       Factura emitida
     * @param nifEmisor     NIF del negocio emisor
     * @param anchoPapelMm  Ancho del papel (58 ó 80). Determina el tamaño del QR.
     * @return Imagen del QR como BufferedImage
     * @throws VerifactuException si no se puede generar el QR
     */
    public static BufferedImage generarQr(Factura factura, String nifEmisor, int anchoPapelMm)
            throws VerifactuException {
        try {
            String url = construirUrl(factura, nifEmisor);
            int tamano = anchoPapelMm <= 58 ? 100 : 150;

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, tamano, tamano);
            BufferedImage imagen = MatrixToImageWriter.toBufferedImage(matrix);

            log.debug("QR generado para factura {}: {}px, url={}", factura.getNumeroCompleto(), tamano, url);
            return imagen;

        } catch (Exception e) {
            throw new VerifactuException("Error al generar el código QR: " + e.getMessage(), e);
        }
    }

    /**
     * Construye la URL de verificación de la AEAT con los parámetros de la factura.
     * Formato: https://www2.agenciatributaria.gob.es/es13/h/verifactu?nif=X&numserie=Y&fecha=Z&importe=W
     *
     * @param factura    Factura emitida
     * @param nifEmisor  NIF del negocio emisor
     * @return URL completa con parámetros codificados
     */
    public static String construirUrl(Factura factura, String nifEmisor) {
        try {
            String numSerie = factura.getNumeroCompleto();
            String fecha = factura.getFecha() != null
                    ? factura.getFecha().format(FMT_FECHA)
                    : "";
            String importe = String.format("%.2f", factura.getTotal()).replace(",", ".");

            return URL_VERIFICACION
                    + "?nif=" + URLEncoder.encode(nifEmisor, StandardCharsets.UTF_8)
                    + "&numserie=" + URLEncoder.encode(numSerie, StandardCharsets.UTF_8)
                    + "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8)
                    + "&importe=" + URLEncoder.encode(importe, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error construyendo URL del QR", e);
            return URL_VERIFICACION;
        }
    }
}
