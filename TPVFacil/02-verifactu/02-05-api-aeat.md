# 02-05 — API AEAT

#verifactu #referencia
Relacionado: [[CLAUDE.md]] | [[02-03-clases-java]] | [[02-04-xml-schema]]

---

## Endpoints

```
Sandbox:    https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP
Producción: https://www1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP
```

**[[06-licencias/06-01-modo-demo|DEMO]]** → sandbox | **[[06-licencias/06-02-activacion-clave|COMPLETO]]** → producción

## Protocolo

SOAP sobre HTTPS. Cabeceras:
```
Content-Type: text/xml; charset=UTF-8
SOAPAction: ""
```

## Respuestas

| Campo | Éxito | Error |
|-------|-------|-------|
| EstadoRegistro | `Correcto` | `Incorrecto` |
| CSV | `ABCD-1234-EFGH-5678` | — |
| CodigoErrorRegistro | — | `1105` |

## Códigos de Error Frecuentes

| Código | Causa | Acción |
|--------|-------|--------|
| 1100 | NIF no válido | Revisar [[03-app-core/03-05-configuracion-negocio]] |
| 1105 | Hash incorrecto | Bug en [[02-03-clases-java#HashChain]] |
| 1200 | Certificado inválido | Revisar [[02-06-certificados]] |
| 1300 | Factura duplicada | Ya enviada — ignorar |
| 5001 | Error AEAT | Reintentar más tarde |

## Estrategia de Reintentos

3 intentos, backoff 2s/4s/8s. Si fallan los 3 → guardar PENDIENTE → reintentar al arrancar.
Ver implementación en [[02-03-clases-java#AeatClient]].
