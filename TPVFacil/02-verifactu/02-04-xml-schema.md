# 02-04 — XML Schema Verifactu

#verifactu #referencia
Relacionado: [[CLAUDE.md]] | [[02-03-clases-java]] | [[02-05-api-aeat]]

---

## Estructura XML SOAP

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <sum:SuministroLR xmlns:sum="https://www2.agenciatributaria.gob.es/...">
      <sum:Cabecera>
        <sum:ObligadoEmision>
          <sum:NombreRazon>NOMBRE NEGOCIO</sum:NombreRazon>
          <sum:NIF>B12345678</sum:NIF>
        </sum:ObligadoEmision>
      </sum:Cabecera>
      <sum:RegistroFacturacion>
        <sum:IDFactura>
          <sum:IDEmisorFactura>B12345678</sum:IDEmisorFactura>
          <sum:NumSerieFactura>A-000123</sum:NumSerieFactura>
          <sum:FechaExpedicionFactura>15-01-2025</sum:FechaExpedicionFactura>
        </sum:IDFactura>
        <sum:DatosFactura>
          <sum:TipoFactura>F2</sum:TipoFactura>
          <sum:ImporteTotal>7.30</sum:ImporteTotal>
        </sum:DatosFactura>
        <sum:HuellaXades>
          <sum:Huella>ABC123...XYZ (64 chars)</sum:Huella>
          <sum:HuellaAnterior>DEF456...UVW</sum:HuellaAnterior>
        </sum:HuellaXades>
        <sum:FirmaFactura>BASE64_FIRMA</sum:FirmaFactura>
      </sum:RegistroFacturacion>
    </sum:SuministroLR>
  </soapenv:Body>
</soapenv:Envelope>
```

## Tipos de Factura

| Código | Tipo |
|--------|------|
| F1 | Factura completa (con datos del cliente) |
| F2 | Factura simplificada / ticket |
| R1 | Rectificativa (devolución) → [[05-tpv-comercio/05-05-devoluciones]] |

## XSD Oficiales

Descargar de la AEAT y guardar en `/app/src/main/resources/verifactu/xsd/`:
```
https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/
aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd
```

Usar para validar con `XmlGenerator.java` antes de enviar → [[02-03-clases-java]].
