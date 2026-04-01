package com.tpvfacil.verifactu;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.verifactu.excepciones.VerifactuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Genera el XML SOAP para enviar a la API Verifactu de la AEAT.
 * Sigue el esquema oficial: SuministroLR.xsd
 *
 * Namespace: https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/
 *            aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd
 */
public class XmlGenerator {

    private static final Logger log = LoggerFactory.getLogger(XmlGenerator.class);

    private static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String NS_SUM  = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd";

    private XmlGenerator() {}

    /**
     * Genera el XML SOAP completo para registrar una factura en Verifactu.
     *
     * @param registro Datos del registro (con hash y firma ya calculados)
     * @return XML SOAP como String
     * @throws VerifactuException si faltan datos obligatorios
     */
    public static String generar(RegistroFactura registro) throws VerifactuException {
        validarDatos(registro);

        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String nombreNegocio = cfg.getNombreNegocio();
        String nif = cfg.getNif();

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope xmlns:soapenv="%s">
              <soapenv:Header/>
              <soapenv:Body>
                <sum:SuministroLR xmlns:sum="%s">
                  <sum:Cabecera>
                    <sum:ObligadoEmision>
                      <sum:NombreRazon>%s</sum:NombreRazon>
                      <sum:NIF>%s</sum:NIF>
                    </sum:ObligadoEmision>
                  </sum:Cabecera>
                  <sum:RegistroFacturacion>
                    <sum:IDFactura>
                      <sum:IDEmisorFactura>%s</sum:IDEmisorFactura>
                      <sum:NumSerieFactura>%s</sum:NumSerieFactura>
                      <sum:FechaExpedicionFactura>%s</sum:FechaExpedicionFactura>
                    </sum:IDFactura>
                    <sum:DatosFactura>
                      <sum:TipoFactura>%s</sum:TipoFactura>
                      <sum:ImporteTotal>%s</sum:ImporteTotal>
                      <sum:Desglose>
                        <sum:DetalleIVA>
                          <sum:BaseImponibleOImporteNoSujeto>%s</sum:BaseImponibleOImporteNoSujeto>
                          <sum:CuotaRepercutida>%s</sum:CuotaRepercutida>
                        </sum:DetalleIVA>
                      </sum:Desglose>
                    </sum:DatosFactura>
                    <sum:HuellaXades>
                      <sum:Huella>%s</sum:Huella>
                      <sum:HuellaAnterior>%s</sum:HuellaAnterior>
                    </sum:HuellaXades>
                    <sum:FirmaFactura>%s</sum:FirmaFactura>
                  </sum:RegistroFacturacion>
                </sum:SuministroLR>
              </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(
                NS_SOAP,
                NS_SUM,
                escaparXml(nombreNegocio),
                escaparXml(nif),
                escaparXml(registro.getIdEmisorFactura()),
                escaparXml(registro.getNumSerieFactura()),
                escaparXml(registro.getFechaExpedicion()),
                escaparXml(registro.getTipoFactura()),
                formatearImporte(registro.getImporteTotal()),
                formatearImporte(registro.getBaseImponible()),
                formatearImporte(registro.getCuotaIva()),
                registro.getHuella(),
                registro.getHuellaAnterior(),
                registro.getFirma()
        );

        log.debug("XML Verifactu generado para: {}", registro.getNumSerieFactura());
        return xml;
    }

    /**
     * Genera el XML SOAP para anular una factura previamente registrada.
     */
    public static String generarAnulacion(RegistroFactura registro) throws VerifactuException {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String nombreNegocio = cfg.getNombreNegocio();
        String nif = cfg.getNif();

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope xmlns:soapenv="%s">
              <soapenv:Header/>
              <soapenv:Body>
                <sum:AnulacionLR xmlns:sum="%s">
                  <sum:Cabecera>
                    <sum:ObligadoEmision>
                      <sum:NombreRazon>%s</sum:NombreRazon>
                      <sum:NIF>%s</sum:NIF>
                    </sum:ObligadoEmision>
                  </sum:Cabecera>
                  <sum:RegistroAnulacion>
                    <sum:IDFactura>
                      <sum:IDEmisorFactura>%s</sum:IDEmisorFactura>
                      <sum:NumSerieFactura>%s</sum:NumSerieFactura>
                      <sum:FechaExpedicionFactura>%s</sum:FechaExpedicionFactura>
                    </sum:IDFactura>
                  </sum:RegistroAnulacion>
                </sum:AnulacionLR>
              </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(
                NS_SOAP,
                NS_SUM,
                escaparXml(nombreNegocio),
                escaparXml(nif),
                escaparXml(registro.getIdEmisorFactura()),
                escaparXml(registro.getNumSerieFactura()),
                escaparXml(registro.getFechaExpedicion())
        );

        return xml;
    }

    private static void validarDatos(RegistroFactura registro) throws VerifactuException {
        if (registro.getIdEmisorFactura() == null || registro.getIdEmisorFactura().isBlank())
            throw new VerifactuException("El NIF del emisor es obligatorio");
        if (registro.getNumSerieFactura() == null || registro.getNumSerieFactura().isBlank())
            throw new VerifactuException("El número de serie es obligatorio");
        if (registro.getFechaExpedicion() == null || registro.getFechaExpedicion().isBlank())
            throw new VerifactuException("La fecha de expedición es obligatoria");
        if (registro.getHuella() == null || registro.getHuella().length() != 64)
            throw new VerifactuException("La huella SHA-256 debe tener 64 caracteres");
        if (registro.getFirma() == null || registro.getFirma().isBlank())
            throw new VerifactuException("La firma digital es obligatoria");
    }

    private static String formatearImporte(java.math.BigDecimal importe) {
        if (importe == null) return "0.00";
        return String.format("%.2f", importe).replace(",", ".");
    }

    /** Escapa los caracteres especiales XML. */
    private static String escaparXml(String texto) {
        if (texto == null) return "";
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
