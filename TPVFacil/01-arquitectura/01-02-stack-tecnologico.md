# 01-02 — Stack Tecnológico

#arquitectura #referencia
Relacionado: [[CLAUDE.md]] | [[01-01-estructura-proyecto]] | [[01-04-configuracion-maven]]

---

## App — `/app`

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 21 LTS | Lenguaje principal |
| JavaFX | 21 | Interfaz gráfica |
| SQLite JDBC | 3.45.x | Base de datos local → [[10-base-de-datos/10-01-esquema-completo]] |
| Maven | 3.9+ | Build y dependencias → [[01-04-configuracion-maven]] |
| jpackage | (Java 14+) | Instalador .exe → [[07-instalador/07-01-jpackage]] |
| JAXB | 4.x | XML Verifactu → [[02-verifactu/02-04-xml-schema]] |
| BouncyCastle | 1.78 | Firma digital → [[02-verifactu/02-06-certificados]] |
| ZXing | 3.5.x | Generación de QR → [[02-verifactu/02-07-qr-ticket]] |
| SLF4J + Logback | 2.x | Logging |

## Web — `/web`

| Tecnología | Uso |
|-----------|-----|
| HTML5 + CSS3 + JS vanilla | Sin frameworks — máxima velocidad |
| Schema.org JSON-LD | SEO semántico → [[09-seo/09-04-seo-tecnico]] |
| GitHub Pages / Netlify | Hosting gratuito |

---

## ¿Por qué Java y no Electron?

| | Java + jpackage | Electron |
|---|---|---|
| Tamaño instalador | ~120 MB (JRE) | ~200-400 MB (Chromium) |
| Integración impresoras | ✅ javax.print nativo | ⚠ Requiere librerías |
| Librerías Verifactu | ✅ JAXB, BouncyCastle | ⚠ Limitado |
| Rendimiento UI | ✅ JavaFX nativo | ⚠ Basado en web |

## ¿Por qué web sin framework?

- Sin compilación → Claude genera HTML directamente ejecutable
- Sin dependencias → carga más rápida → mejor SEO
- Sin mantenimiento → cualquier cambio es editar un HTML
- Hosting gratuito en GitHub Pages / Netlify

---

## El Instalador es Autónomo

`jpackage` incluye la JRE dentro del instalador. El usuario **no necesita instalar Java**.
El instalador pesa ~120 MB e incluye todo lo necesario.
Ver detalles en [[07-instalador/07-01-jpackage]].
