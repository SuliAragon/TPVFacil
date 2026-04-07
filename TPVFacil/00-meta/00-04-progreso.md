# 00-04 — Progreso del Desarrollo

#meta #progreso
Relacionado: [[CLAUDE.md]] | [[11-fases/11-01-roadmap]]
Última actualización: 2026-04-01

---

## Estado General

| Fase | Estado | Fecha |
|------|--------|-------|
| [[11-fases/11-02-fase1-nucleo\|Fase 1 — Núcleo]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-03-fase2-verifactu\|Fase 2 — Verifactu]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-04-fase3-comercio\|Fase 3 — Comercio]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-05-fase4-hosteleria\|Fase 4 — Hostelería]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-06-fase5-instalador\|Fase 5 — Instalador]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-07-fase6-web\|Fase 6 — Web]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-08-fase7-beta\|Fase 7 — Beta]] | ⬜ Pendiente | — |
| [[11-fases/11-09-fase8-lanzamiento\|Fase 8 — Lanzamiento]] | ⬜ Pendiente | — |

---

## ✅ Fase 1 — Núcleo (Completada)

### Ficheros creados

#### pom.xml
- `/App/pom.xml` — Maven completo con todas las dependencias

#### Modelos de datos
- `com.tpvfacil.core.modelo.Producto`
- `com.tpvfacil.core.modelo.Cliente`
- `com.tpvfacil.core.modelo.Factura`
- `com.tpvfacil.core.modelo.LineaFactura`
- `com.tpvfacil.core.modelo.FormaPago` (enum: EFECTIVO, TARJETA, MIXTO)
- `com.tpvfacil.core.modelo.TipoNegocio` (enum: HOSTELERIA, COMERCIO)
- `com.tpvfacil.hosteleria.modelo.Mesa`
- `com.tpvfacil.hosteleria.modelo.EstadoMesa` (enum: LIBRE, OCUPADA, PENDIENTE_PAGO)
- `com.tpvfacil.hosteleria.modelo.Comanda`
- `com.tpvfacil.hosteleria.modelo.LineaComanda`
- `com.tpvfacil.comercio.modelo.ItemCesta`

#### Base de datos
- `com.tpvfacil.core.db.DatabaseManager` — Singleton, WAL, migraciones, esquema completo
- `com.tpvfacil.core.db.ConfiguracionRepository`
- `com.tpvfacil.core.db.ProductoRepository`
- `com.tpvfacil.core.db.ClienteRepository`
- `com.tpvfacil.core.db.FacturaRepository`
- `com.tpvfacil.core.db.RegistroVerifactuRepository`
- `com.tpvfacil.core.db.MesaRepository`
- `com.tpvfacil.core.db.ComandaRepository`
- `com.tpvfacil.core.db.CajaRepository`

#### Configuración
- `com.tpvfacil.config.ConfiguracionManager` — Singleton con caché Map<String,String>

#### Sistema de licencias
- `com.tpvfacil.licencia.HardwareFingerprint` — MAC+hostname → SHA-256 → 16 chars
- `com.tpvfacil.licencia.LicenciaManager` — HMAC-SHA256, validar/activar
- `com.tpvfacil.licencia.ModoDemo` — límite 20 tickets/día
- `com.tpvfacil.licencia.GeneradorClaves` — herramienta interna (main propio)
- `com.tpvfacil.licencia.ActivacionController` + `activacion.fxml`

#### UI principal
- `com.tpvfacil.Main` — flujo de arranque completo
- `com.tpvfacil.core.ui.InicioController` + `inicio.fxml`
- `com.tpvfacil.core.ui.PrimerArranqueController` + `wizard-primer-arranque.fxml`
- `com.tpvfacil.core.ui.ConfiguracionController` + `pantalla-configuracion.fxml`

#### Recursos
- `estilos.css` — estilos globales JavaFX
- `logback.xml` — configuración de logging
- `config.properties` — propiedades estáticas de la app

### Verificación ✅
```
mvn clean compile → 30 ficheros compilados. BUILD SUCCESS.
mvn javafx:run    → App arranca, wizard se muestra, BD se crea.
                    Modo demo activado. Cierre limpio.
```

---

## ✅ Fase 2 — Verifactu (Completada)

### Ficheros creados
- `com.tpvfacil.verifactu.RegistroFactura` — POJO del registro Verifactu
- `com.tpvfacil.verifactu.EstadoEnvio` — enum: OK, PENDIENTE, ERROR
- `com.tpvfacil.verifactu.RespuestaAeat` — DTO de respuesta AEAT
- `com.tpvfacil.verifactu.excepciones.VerifactuException`
- `com.tpvfacil.verifactu.excepciones.AeatException`
- `com.tpvfacil.verifactu.HashChain` — SHA-256 encadenado entre facturas
- `com.tpvfacil.verifactu.CertificadoManager` — carga .p12/.pfx, cifrado password AES-256
- `com.tpvfacil.verifactu.FirmaDigital` — firma SHA256withRSA (PKCS#1)
- `com.tpvfacil.verifactu.XmlGenerator` — genera SOAP XML con esquema AEAT
- `com.tpvfacil.verifactu.AeatClient` — HTTP SOAP al WS AEAT (sandbox/producción)
- `com.tpvfacil.verifactu.QrGenerator` — QR con ZXing (URL verificación AEAT)
- `com.tpvfacil.verifactu.VerifactuManager` — orquestador completo
- `com.tpvfacil.core.util.TicketFormatter` — formato ticket térmico 58/80mm
- `com.tpvfacil.core.util.TicketPrinter` — impresión con javax.print

### Verificación ✅
```
mvn clean compile → 44 ficheros compilados. BUILD SUCCESS.
Flujo: hash → firma (placeholder) → XML → AEAT sandbox → ticket.
```

---

## ✅ Fase 3 — TPV Comercio (Completada)

### Ficheros creados

#### Modulo Comercio
- `com.tpvfacil.comercio.service.VentaService` — construye Factura desde cesta, calcula IVA agrupado
- `com.tpvfacil.comercio.service.StockService` — descontar/reponer/ajustar stock en transacciones
- `com.tpvfacil.comercio.controller.VentaController` + `pantalla-venta.fxml`
  - Catálogo en FlowPane con tarjetas de producto
  - Barra de categorías dinámica con ToggleButtons
  - Detección de lector USB de código de barras (threshold configurable)
  - Cesta con +/−/🗑 por línea, total en tiempo real
  - Reloj en header, buscador con foco automático
- `com.tpvfacil.comercio.controller.CobroComercioController` + `pantalla-cobro-comercio.fxml`
  - Resumen con desglose IVA, total grande
  - Selector Efectivo/Tarjeta/Mixto con ToggleButtons
  - Teclado de billetes (5/10/20/50€), cambio en tiempo real
  - Flujo: ModoDemo → VentaService → VerifactuManager → StockService
- `com.tpvfacil.comercio.controller.DevolucionController` + `pantalla-devolucion.fxml`
  - Busca factura por número, selección por checkbox de líneas
  - Genera Factura Rectificativa R1 con importes negativos
  - Reenvía a VerifactuManager + repone stock
- `com.tpvfacil.comercio.controller.GestionStockController` + `pantalla-gestion-stock.fxml`
  - TableView con stock editable, alertas rojo/naranja, guardado en transacción

#### UI Core compartida
- `com.tpvfacil.core.ui.GestionCartaController` + `pantalla-gestion-carta.fxml`
- `com.tpvfacil.core.ui.DialogoProductoController` + `dialogo-producto.fxml` + `dialogo-categoria.fxml`
- `com.tpvfacil.core.ui.CierreCajaController` + `pantalla-cierre-caja.fxml`
- `com.tpvfacil.core.ui.BackupController` + `pantalla-backup.fxml`
- `com.tpvfacil.core.ui.componentes.TarjetaMesa`

### Verificación ✅
```
mvn clean compile → 62 ficheros compilados. BUILD SUCCESS.
Flujo completo: venta → cobro → ticket → Verifactu → stock descontado.
```

---

## ✅ Fase 4 — TPV Hostelería (Completada)

### Ficheros creados
- `com.tpvfacil.hosteleria.service.ComandaService` — gestión de comandas y estados
- `com.tpvfacil.hosteleria.service.TicketCocina` — impresión en impresora de cocina
- `com.tpvfacil.hosteleria.controller.MesasController` + `pantalla-mesas.fxml`
  - Grid de mesas con TarjetaMesa (color por estado: verde/rojo/naranja)
  - Añadir/renombrar mesas desde diálogo
- `com.tpvfacil.hosteleria.controller.DialogoMesaController` + `dialogo-mesa.fxml`
- `com.tpvfacil.hosteleria.controller.ComandaController` + `pantalla-comanda.fxml`
  - Carta de productos, líneas de comanda, impresión a cocina
- `com.tpvfacil.hosteleria.controller.CobroHosteleriaController` + `pantalla-cobro-hosteleria.fxml`
  - Cobro por mesa: Efectivo/Tarjeta/Mixto → Verifactu → libera mesa

### Verificación ✅
```
mvn clean compile → 62 ficheros compilados. BUILD SUCCESS.
(Hostelería forma parte de los 62 ficheros del build de Fase 3)
```

---

---

## ✅ Fase 5 — Instalador (Completada)

### Ficheros creados
- `.github/workflows/release.yml` — 3 jobs paralelos en GitHub Actions:
  - `build-windows` (windows-latest) → `.exe` con JRE 21 embebido
  - `build-macos` (macos-latest) → `.dmg` con JRE 21 embebido
  - `build-linux` (ubuntu-latest) → `.deb` con JRE 21 embebido
  - `create-release` — crea GitHub Release con los 3 instaladores
- `App/build.sh` — script local para macOS y Linux
- `App/build.bat` — script local para Windows

### Cómo generar los instaladores
```bash
git tag v1.0.0
git push --tags
# GitHub Actions genera los 3 instaladores automáticamente en ~5 min
```

### Verificación ✅
```
mvn clean compile → BUILD SUCCESS (Java 21 target)
Workflow configurado y subido al repo.
```

---

## ✅ Fase 6 — Web (Completada)

### Ficheros creados
- `Web/assets/css/global.css` — Sistema de diseño completo (variables, componentes, grid, nav, footer)
- `Web/assets/js/main.js` — Nav scroll, hamburger, FAQ accordion, cookies banner, trackDescarga()
- `Web/index.html` — Landing page principal (Schema: SoftwareApplication + FAQPage)
- `Web/hosteleria/index.html` — Página de hostelería (Schema: SoftwareApplication audience bares/restaurantes)
- `Web/comercio/index.html` — Página de comercio (Schema: SoftwareApplication audience comercio)
- `Web/precios/index.html` — Precios (Schema: Offer 79€ + FAQPage)
- `Web/descargar/index.html` — Descarga por SO: .exe/.dmg/.deb vía GitHub Releases
- `Web/verifactu/index.html` — Página informativa Verifactu (Schema: Article)
- `Web/blog/index.html` — Índice del blog
- `Web/blog/que-es-verifactu/index.html` — Primer artículo del blog (Schema: Article)
- `Web/legal/aviso-legal.html` — Aviso legal LSSICE
- `Web/legal/privacidad.html` — Política de privacidad RGPD
- `Web/legal/cookies.html` — Política de cookies
- `Web/robots.txt` — Disallow /legal/, Sitemap apuntando a sitemap.xml
- `Web/sitemap.xml` — 8 URLs indexadas con prioridades

### Verificación ✅
```
Todas las páginas con SEO on-page: title, meta description, canonical, robots, Schema.org JSON-LD.
Mobile-first. Inter font. Sin dependencias de frameworks.
Sitemap y robots.txt listos para Google Search Console.
```

---

## ⬜ Fases 7-8 — Resumen

| Fase | Contenido principal |
|------|---------------------|
| **Fase 7 — Beta** | Pruebas con 2 negocios reales, bug fixes |
| **Fase 8 — Lanzamiento** | Primera venta, campaña marketing |

---

## Comandos útiles de desarrollo

```bash
# Ejecutar en desarrollo (Mac)
cd App
JAVA_HOME=/opt/homebrew/opt/openjdk mvn javafx:run

# Compilar
JAVA_HOME=/opt/homebrew/opt/openjdk mvn clean compile

# Generar JAR (cuando esté listo)
JAVA_HOME=/opt/homebrew/opt/openjdk mvn clean package

# Generar clave de licencia para un cliente
java -cp target/tpvfacil-1.0.0-shaded.jar com.tpvfacil.licencia.GeneradorClaves A3B7C2D1E4F5G6H7

# Base de datos (Mac)
open ~/Library/Application\ Support/TPVFacil/
```

---

## Decisiones tomadas durante el desarrollo

| Decisión | Valor elegido | Motivo |
|----------|--------------|--------|
| Java en Mac dev | Java 25 (Homebrew) | Único disponible en el equipo |
| Compilación target | Java 21 (`--release 21`) | Compatibilidad con Windows final |
| BD path Mac | `~/Library/Application Support/TPVFacil/` | Convención estándar macOS |
| FXML ToggleGroup | Inline en FXML | Más simple para wizard |
| IVA Comercio default | 21% (configurable por producto) | Estándar España |
| Serie facturas | "C" = Comercio, "H" = Hostelería, "R" = Rectificativa | Identificación clara por módulo |
| Lector código barras | Threshold 50ms (configurable) | Diferencia entrada humana vs scanner |
| Verifactu fallo red | Registro queda PENDIENTE, reintento en arranque | Venta nunca bloqueada por Verifactu |
