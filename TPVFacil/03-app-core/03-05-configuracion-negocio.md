# 03-05 — Configuración del Negocio

#app-core #fase-1
Relacionado: [[CLAUDE.md]] | [[03-02-base-de-datos-sqlite]] | [[02-verifactu/02-06-certificados]] | [[06-licencias/06-04-hardware-fingerprint]]
Estado: #pendiente

---

## Claves de Configuración

| Clave | Descripción | Obligatorio |
|-------|-------------|-------------|
| `nombre_negocio` | Nombre fiscal | ✅ |
| `nif` | NIF/CIF | ✅ |
| `direccion` | Dirección completa | ✅ |
| `ruta_certificado` | Ruta al .p12/.pfx | Para producción |
| `password_certificado` | Password cifrada | Para producción |
| `ancho_papel_mm` | 58 ó 80 | Default: 80 |
| `impresora_tickets` | Nombre impresora | Opcional |
| `impresora_cocina` | Solo hostelería | Opcional |
| `web_url_compra` | URL botón "Comprar" | Default en config.properties |
| `db_version` | Versión esquema BD | Automático |
| `app_version` | Versión de la app | Automático |

## Wizard Primera Ejecución (3 pasos)

```
Paso 1: Datos del negocio (nombre, NIF, dirección)
        + Validación de NIF español (algoritmo dígito control)

Paso 2: Certificado Verifactu (opcional — puede saltarse)
        → [[02-verifactu/02-06-certificados]]

Paso 3: Impresoras (opcional)
        → Seleccionar impresora tickets + cocina + ancho papel
```

## Pantalla de Configuración Permanente

4 tabs accesibles desde el menú ⚙️:
- **Datos del negocio**
- **Verifactu** → certificado + test de envío al sandbox
- **Impresión** → selectores + test de impresión
- **Licencia** → estado, ID equipo ([[06-licencias/06-04-hardware-fingerprint]]), campo clave

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera:
1. ConfiguracionManager.java en com.tpvfacil.config
   Singleton con cache Map<String,String>
   Métodos: get/set, getNombreNegocio(), getNif(), getAnchoPapelMm(), isConfigurado(), etc.

2. wizard-primer-arranque.fxml + PrimerArranqueController.java
   3 pasos como se describe. Validar NIF (letra control). Paso 2 y 3 son opcionales.

3. pantalla-configuracion.fxml + ConfiguracionController.java
   4 tabs. Tab Verifactu incluye botón "Test sandbox" que envía factura ficticia.
   Tab Impresión incluye botón "Imprimir ticket de prueba".
```
