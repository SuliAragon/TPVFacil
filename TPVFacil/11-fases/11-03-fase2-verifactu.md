# 11-03 — Fase 2: Verifactu

#fase #fase-2
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[02-verifactu/02-02-flujo-tecnico]] | [[02-verifactu/02-03-clases-java]]
Prerequisito: [[11-02-fase1-nucleo|Fase 1 ✅]]
Estado: #pendiente

---

## Criterio de Éxito

- ✅ `VerifactuManager.procesarFactura(test)` ejecuta sin errores
- ✅ AEAT sandbox devuelve `EstadoRegistro: Correcto` con CSV
- ✅ El hash SHA-256 se encadena correctamente
- ✅ XML valida contra XSD oficial de la AEAT
- ✅ Ticket impreso incluye QR con URL de verificación
- ✅ Si falla el envío → PENDIENTE → se reintenta al reiniciar

---

## Prerequisitos Externos

- Certificado digital disponible (real o autofirmado para pruebas)
- Descargar XSD oficiales de la AEAT → guardar en `/app/src/main/resources/verifactu/xsd/`

---

## Tareas

### 2.1 — Modelos y excepciones
```
Lee [[CLAUDE.md]] y [[02-verifactu/02-03-clases-java]].
Genera: RegistroFactura, EstadoEnvio (enum), RespuestaAeat,
VerifactuException, AeatException
```

### 2.2 — HashChain (con tests)
```
Lee [[CLAUDE.md]] y sección HashChain de [[02-verifactu/02-03-clases-java]].
Genera HashChain.java + HashChainTest.java (JUnit 5).
Tests: primera factura usa 64 ceros, encadenamiento correcto, hash cambia si cambia un campo.
```

### 2.3 — CertificadoManager
```
Lee [[CLAUDE.md]] y [[02-verifactu/02-06-certificados]].
Genera CertificadoManager.java con cifrado AES-256.
```

### 2.4 — FirmaDigital + XmlGenerator + AeatClient
```
Lee [[CLAUDE.md]], [[02-verifactu/02-03-clases-java]], [[02-verifactu/02-04-xml-schema]] y [[02-verifactu/02-05-api-aeat]].
Genera las tres clases. XmlGenerator valida contra XSD.
AeatClient con 3 reintentos y backoff.
```

### 2.5 — QrGenerator + TicketFormatter + TicketPrinter
```
Lee [[CLAUDE.md]], [[02-verifactu/02-07-qr-ticket]] y [[03-app-core/03-07-impresion-tickets]].
Genera las tres clases.
```

### 2.6 — VerifactuManager (orquestador)
```
Lee [[CLAUDE.md]] y [[02-verifactu/02-02-flujo-tecnico]].
Genera VerifactuManager.java con procesarFactura() y reenviarPendientes().
```

### 2.7 — Tab Verifactu en configuración
```
Lee [[CLAUDE.md]] y [[03-app-core/03-05-configuracion-negocio]].
Implementa el tab Verifactu en la pantalla de configuración con test de envío al sandbox.
```

---

## Verificación

```
1. Configurar certificado (autofirmado sirve para sandbox)
2. Pulsar "Test de envío sandbox" → respuesta Correcto + CSV
3. Desconectar internet → emitir ticket → estado PENDIENTE
4. Reconectar → reiniciar app → estado cambia a OK
```
