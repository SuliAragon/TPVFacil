package com.tpvfacil.verifactu;

import com.tpvfacil.verifactu.excepciones.AeatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP para comunicarse con la API SOAP de Verifactu (AEAT).
 * Gestiona reintentos con backoff exponencial: 3 intentos, esperas 2s/4s/8s.
 *
 * Sandbox:    https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP
 * Producción: https://www1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP
 */
public class AeatClient {

    private static final Logger log = LoggerFactory.getLogger(AeatClient.class);

    private static final String URL_SANDBOX    = "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP";
    private static final String URL_PRODUCCION = "https://www1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP";

    private static final int MAX_REINTENTOS    = 3;
    private static final long[] ESPERAS_MS     = {2000, 4000, 8000};
    private static final Duration TIMEOUT_CONEXION  = Duration.ofSeconds(10);
    private static final Duration TIMEOUT_RESPUESTA = Duration.ofSeconds(30);

    private final HttpClient httpClient;

    public AeatClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT_CONEXION)
                .build();
    }

    /**
     * Envía un XML SOAP a la AEAT y devuelve la respuesta parseada.
     * Reintenta hasta 3 veces con backoff exponencial si hay error de red.
     *
     * @param xmlSoap         XML SOAP generado por {@link XmlGenerator}
     * @param modoProduccion  true = producción AEAT, false = sandbox
     * @return {@link RespuestaAeat} con el resultado
     * @throws AeatException si fallan todos los reintentos
     */
    public RespuestaAeat enviar(String xmlSoap, boolean modoProduccion) throws AeatException {
        String url = modoProduccion ? URL_PRODUCCION : URL_SANDBOX;
        String entorno = modoProduccion ? "PRODUCCIÓN" : "SANDBOX";

        log.info("Enviando factura a AEAT [{}]...", entorno);

        AeatException ultimaExcepcion = null;

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                RespuestaAeat respuesta = ejecutarPeticion(xmlSoap, url);

                if (respuesta.isCorrecto()) {
                    log.info("AEAT [{}] acepta la factura. CSV: {}", entorno, respuesta.getCsv());
                    return respuesta;
                }

                // Si la AEAT devuelve un error de negocio (no de red), no reintentamos
                if (respuesta.esDuplicada()) {
                    log.warn("Factura duplicada en AEAT (código 1300) — ya estaba registrada");
                    return respuesta;
                }

                log.warn("AEAT responde con error [{}]: {} — {}",
                        respuesta.getCodigoError(), respuesta.getDescripcionError(),
                        intento < MAX_REINTENTOS ? "reintentando..." : "sin más reintentos");

                ultimaExcepcion = new AeatException(
                        "AEAT rechazó la factura: " + respuesta.getDescripcionError(),
                        respuesta.getCodigoError());

                // Errores de negocio como NIF inválido no tienen sentido reintentar
                if (esErrorNegocio(respuesta.getCodigoError())) {
                    throw ultimaExcepcion;
                }

            } catch (AeatException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Error de red al contactar con AEAT (intento {}/{}): {}",
                        intento, MAX_REINTENTOS, e.getMessage());
                ultimaExcepcion = new AeatException("Error de comunicación con AEAT: " + e.getMessage(), e);
            }

            // Espera antes de reintentar
            if (intento < MAX_REINTENTOS) {
                esperarBackoff(intento);
            }
        }

        throw ultimaExcepcion != null ? ultimaExcepcion
                : new AeatException("Error desconocido al comunicar con la AEAT");
    }

    private RespuestaAeat ejecutarPeticion(String xmlSoap, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT_RESPUESTA)
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", "")
                .POST(HttpRequest.BodyPublishers.ofString(xmlSoap))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.debug("AEAT HTTP status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            throw new AeatException("AEAT devolvió HTTP " + response.statusCode());
        }

        return parsearRespuesta(response.body());
    }

    /**
     * Parsea el XML de respuesta SOAP de la AEAT.
     * Busca los elementos EstadoRegistro y CSV (o CodigoErrorRegistro).
     */
    private RespuestaAeat parsearRespuesta(String xmlRespuesta) {
        RespuestaAeat respuesta = new RespuestaAeat();
        respuesta.setXmlRespuesta(xmlRespuesta);

        // Extraer EstadoRegistro
        String estado = extraerElemento(xmlRespuesta, "EstadoRegistro");
        if ("Correcto".equalsIgnoreCase(estado)) {
            respuesta.setCorrecto(true);
            respuesta.setCsv(extraerElemento(xmlRespuesta, "CSV"));
        } else {
            respuesta.setCorrecto(false);
            respuesta.setCodigoError(extraerElemento(xmlRespuesta, "CodigoErrorRegistro"));
            respuesta.setDescripcionError(extraerElemento(xmlRespuesta, "DescripcionErrorRegistro"));
        }

        return respuesta;
    }

    /** Extrae el valor de un elemento XML (búsqueda simple por nombre de tag). */
    private String extraerElemento(String xml, String nombreTag) {
        String abierto = "<" + nombreTag + ">";
        String cerrado = "</" + nombreTag + ">";
        int inicio = xml.indexOf(abierto);
        if (inicio < 0) {
            // Probar con namespace
            inicio = xml.indexOf(":" + nombreTag + ">");
            if (inicio < 0) return null;
            inicio = xml.indexOf(">", inicio) + 1;
        } else {
            inicio += abierto.length();
        }
        int fin = xml.indexOf(cerrado, inicio);
        if (fin < 0) {
            fin = xml.indexOf("</" , inicio);
        }
        if (fin < 0) return null;
        return xml.substring(inicio, fin).trim();
    }

    /** Espera el tiempo de backoff correspondiente al intento dado. */
    private void esperarBackoff(int intento) {
        long espera = intento <= ESPERAS_MS.length ? ESPERAS_MS[intento - 1] : ESPERAS_MS[ESPERAS_MS.length - 1];
        log.debug("Esperando {}ms antes de reintentar...", espera);
        try {
            Thread.sleep(espera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Indica si el código de error de la AEAT es un error de negocio (no vale la pena reintentar). */
    private boolean esErrorNegocio(String codigo) {
        // 1100 NIF inválido, 1105 hash incorrecto, 1200 certificado inválido
        return "1100".equals(codigo) || "1105".equals(codigo) || "1200".equals(codigo);
    }
}
