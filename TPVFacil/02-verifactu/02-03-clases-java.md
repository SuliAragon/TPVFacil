# 02-03 — Clases Java Verifactu

#verifactu #implementacion
Relacionado: [[CLAUDE.md]] | [[02-02-flujo-tecnico]] | [[01-arquitectura/01-03-estructura-paquetes-java]]
Estado: #pendiente

---

## Mapa de Clases

```
com.tpvfacil.verifactu
├── VerifactuManager.java     ← Orquestador → [[02-02-flujo-tecnico]]
├── RegistroFactura.java      ← Modelo de datos
├── HashChain.java            ← SHA-256 encadenado
├── FirmaDigital.java         ← Firma con BouncyCastle → [[02-06-certificados]]
├── XmlGenerator.java         ← XML según AEAT → [[02-04-xml-schema]]
├── AeatClient.java           ← HTTP a la AEAT → [[02-05-api-aeat]]
├── CertificadoManager.java   ← Gestión .p12/.pfx → [[02-06-certificados]]
├── QrGenerator.java          ← QR del ticket → [[02-07-qr-ticket]]
└── excepciones/
    ├── VerifactuException.java
    └── AeatException.java
```

---

## RegistroFactura.java

Campos principales:
```java
String idEmisorFactura;     // NIF del negocio
String numSerieFactura;     // "A-000123"
String fechaExpedicion;     // "dd-MM-yyyy"
String tipoFactura;         // "F2" (ticket) o "F1" (factura completa)
BigDecimal baseImponible;
BigDecimal cuotaIva;
BigDecimal importeTotal;
String huella;              // Hash SHA-256 calculado
String huellaAnterior;      // Hash de la factura anterior
String firma;               // Base64
String csvAeat;             // Código de la AEAT
EstadoEnvio estado;         // OK, PENDIENTE, ERROR
```

## HashChain.java

```java
// Concatenar: IDEmisor|NumSerie|Fecha|Tipo|CuotaTotal|ImporteTotal|HashAnterior
// Aplicar SHA-256 → hexadecimal uppercase (64 chars)
// Primera factura: hashAnterior = "000...000" (64 ceros)
public String calcularHash(RegistroFactura registro, String hashAnterior)
```

## FirmaDigital.java (BouncyCastle)

```java
// Algoritmo: SHA256withRSA
// Entrada: contenido String + KeyStore + alias + password
// Salida: firma en Base64
public String firmar(String contenido, KeyStore keyStore, String alias, String password)
```

## AeatClient.java (Java HttpClient nativo)

```java
// Timeout conexión: 10s, respuesta: 30s
// Reintentos: 3, backoff: 2s/4s/8s
// Si falla → lanza AeatException
public RespuestaAeat enviar(String xmlSoap, boolean modoProduccion)
```

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[02-02-flujo-tecnico]] y este documento.

Genera TODAS las clases en com.tpvfacil.verifactu en este orden:
1. RegistroFactura.java + EstadoEnvio.java (enum) + RespuestaAeat.java
2. VerifactuException.java + AeatException.java
3. HashChain.java — con test unitario HashChainTest.java
4. CertificadoManager.java → ver [[02-06-certificados]]
5. FirmaDigital.java — usa BouncyCastle
6. XmlGenerator.java — valida contra XSD → ver [[02-04-xml-schema]]
7. AeatClient.java — con reintentos → ver [[02-05-api-aeat]]
8. QrGenerator.java → ver [[02-07-qr-ticket]]
9. VerifactuManager.java — orquesta todo
```
