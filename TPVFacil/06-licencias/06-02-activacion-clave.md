# 06-02 — Activación de Licencia

#licencias #fase-1
Relacionado: [[CLAUDE.md]] | [[06-01-modo-demo]] | [[06-04-hardware-fingerprint]] | [[06-03-opciones-plataforma-pago]]
Estado: #pendiente

---

## Formato de Clave

```
TPVF-XXXX-XXXX-XXXX-XXXX
```
4 grupos de 4 caracteres alfanuméricos (A-Z, 0-9).

## Algoritmo de Validación (sin servidor)

```java
// La clave = HMAC-SHA256(hardware_fingerprint, clave_secreta_embebida)
// truncado y formateado como TPVF-XXXX-XXXX-XXXX-XXXX

boolean valida = LicenciaManager.validarClave(
    claveIntroducida,
    HardwareFingerprint.generar()
);
```

Ver hardware fingerprint en [[06-04-hardware-fingerprint]].

## Pantalla de Activación

```
┌─────────────────────────────────────────┐
│         Activar TPVFácil                │
│                                         │
│  ID de tu equipo: A3B7C2D1E4F5G6H7     │
│  [📋 Copiar]                            │
│                                         │
│  "Proporciona este código al comprar    │
│   en tpvfacil.es/precios"              │
│                                         │
│  Clave de licencia:                     │
│  [TPVF-____-____-____-____]            │
│                                         │
│  [ Activar ]  [ Ir a comprar → ]       │
└─────────────────────────────────────────┘
```

## Herramienta Interna GeneradorClaves.java

```java
// main() propio — NO forma parte de la app principal
// Uso: java GeneradorClaves A3B7C2D1E4F5G6H7
// Salida: TPVF-X3K9-M2PQ-7RBN-X4WZ
```

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[06-04-hardware-fingerprint]] y este documento.

Genera:
1. LicenciaManager.java — isModoDemo(), validarClave(), activar(), guardarLicencia()
2. ActivacionController.java + activacion.fxml
   Mostrar fingerprint + botón Copiar (Clipboard JavaFX)
   Validar clave al introducir → si válida: activar y reiniciar app
   Botón "Ir a comprar" → Desktop.browse(URL desde ConfiguracionManager)
3. GeneradorClaves.java (main propio, herramienta interna)
   Argumento: hardware fingerprint → imprime la clave correspondiente
```
