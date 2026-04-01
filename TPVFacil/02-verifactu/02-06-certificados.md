# 02-06 — Certificados Digitales

#verifactu #configuracion
Relacionado: [[CLAUDE.md]] | [[02-03-clases-java]] | [[03-app-core/03-05-configuracion-negocio]]

---

## ¿Qué Certificado Necesita el Negocio?

El mismo certificado digital que usan para presentar el IVA online.
Formato: `.p12` o `.pfx` (PKCS#12 — contiene clave privada + certificado público).

## Flujo de Configuración

```
Configuración → tab Verifactu
  → Seleccionar archivo .p12/.pfx
  → Introducir contraseña
  → TPVFácil valida: muestra titular, NIF, fecha expiración
  → Guardar (contraseña se cifra con AES-256)
```

Ver pantalla en [[03-app-core/03-05-configuracion-negocio]].

## Almacenamiento Seguro

- **Ruta** del .p12 → ConfiguracionRepository (texto plano — es solo una ruta)
- **Contraseña** → cifrada AES-256 con clave derivada del [[06-licencias/06-04-hardware-fingerprint|hardware fingerprint]]
- **Nunca** en texto plano

## Alertas de Expiración

Al arrancar: si expira en menos de 30 días → banner naranja de aviso.
Si ya expirado → bloquear modo producción, solo sandbox permitido.

## Para Desarrollo (sin certificado real)

```bash
# Generar certificado autofirmado con OpenSSL
openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes
openssl pkcs12 -export -out pruebas.p12 -inkey key.pem -in cert.pem
# Password: cualquiera para pruebas
```

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera CertificadoManager.java en com.tpvfacil.verifactu:
- cargarCertificado(Path rutaP12, String password): KeyStore
- validarCertificado(KeyStore ks): lanza excepción si no válido
- fechaExpiracion(KeyStore ks): LocalDate
- estaProximoAVencer(KeyStore ks, int dias): boolean
- cifrarPassword(String pw) / descifrarPassword(String cifrada)
  Usar AES-256, clave = SHA-256(HardwareFingerprint.generar() + "salt_tpvfacil")
- Cachear el KeyStore en memoria para no releer en cada factura
```
