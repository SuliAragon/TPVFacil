# 06-04 — Hardware Fingerprint

#licencias #fase-1
Relacionado: [[CLAUDE.md]] | [[06-02-activacion-clave]]
Estado: #pendiente

---

## Generación

```java
// HardwareFingerprint.java
// Combinar: MAC address (primera interfaz no-loopback) + hostname
// Aplicar SHA-256 → primeros 16 chars en mayúsculas
// Resultado: "A3B7C2D1E4F5G6H7" (siempre 16 chars)

// Fallback si falla:
// UUID aleatorio guardado en ConfiguracionManager ("hw_fingerprint_fallback")
// Persiste aunque cambie el hardware
```

## Uso

- Se muestra en [[06-02-activacion-clave|la pantalla de activación]] para que el usuario lo copie al comprar
- El vendedor (tú) usa `GeneradorClaves.java` con ese fingerprint para generar la clave válida
- La clave solo funciona en ese equipo concreto

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera HardwareFingerprint.java en com.tpvfacil.licencia:
- Método estático String generar()
- MAC address de la primera interfaz no-loopback activa
- SHA-256(mac + "|" + hostname) → primeros 16 chars uppercase
- Fallback con UUID guardado en ConfiguracionManager
- El resultado tiene SIEMPRE exactamente 16 caracteres
```
