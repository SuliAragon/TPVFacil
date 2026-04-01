# 02-02 — Flujo Técnico Verifactu

#verifactu #core
Relacionado: [[CLAUDE.md]] | [[02-01-que-es-verifactu]] | [[02-03-clases-java]] | [[11-fases/11-03-fase2-verifactu]]
Estado: #pendiente

---

## Flujo Completo (cada vez que se cobra)

```
[Usuario pulsa "Cobrar"]
        │
        ▼
1. Crear objeto Factura con todos los datos
        │
        ▼
2. Guardar Factura en SQLite → [[10-base-de-datos/10-01-esquema-completo]]
        │
        ▼
3. HashChain.calcularHash(factura, hashAnterior)
   → Primera factura: hashAnterior = 64 ceros
   → Resto: hashAnterior = hash de la factura anterior
        │
        ▼
4. FirmaDigital.firmar(registro, certificado)
   → Certificado cargado desde [[02-06-certificados]]
        │
        ▼
5. XmlGenerator.generar(registro, firma)
   → XML según esquema → [[02-04-xml-schema]]
   → Validar contra XSD antes de enviar
        │
        ▼
6. AeatClient.enviar(xml)
   → [[06-licencias/06-01-modo-demo|DEMO]] → sandbox AEAT
   → [[06-licencias/06-02-activacion-clave|COMPLETO]] → producción AEAT
   → Ver endpoint en [[02-05-api-aeat]]
   → Si falla: reintentar 3 veces → guardar PENDIENTE
        │
        ▼
7. Guardar resultado en registros_verifactu (csv_aeat, estado)
        │
        ▼
8. Generar ticket → [[03-app-core/03-07-impresion-tickets]]
   → QR con URL verificación → [[02-07-qr-ticket]]
   → Marca de agua si DEMO
        │
        ▼
9. Imprimir ticket
```

## Gestión de Errores

| Situación | Comportamiento |
|-----------|---------------|
| Sin internet | Guardar PENDIENTE → reenviar al arrancar |
| AEAT devuelve error | Registrar error, mostrar aviso, no bloquear venta |
| Error en firma | Error crítico → no emitir factura |
| Factura duplicada (código 1300) | Ignorar → ya estaba registrada |

## Prompt para Claude

```
Lee [[CLAUDE.md]], este documento y [[02-03-clases-java]].

Implementa VerifactuManager.java en com.tpvfacil.verifactu:
- Método principal: ResultadoCobro procesarFactura(Factura factura)
- Orquesta los pasos 1-9 del flujo
- Método reenviarPendientes(): llamado al arrancar desde [[03-app-core/03-01-main-java]]
- En MODO DEMO: sandbox. En MODO COMPLETO: producción.
- Si el envío falla: la venta se completa igualmente, registro queda PENDIENTE
- Javadoc en español
```
