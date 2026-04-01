# 03-06 — Cierre de Caja

#app-core
Relacionado: [[CLAUDE.md]] | [[10-base-de-datos/10-01-esquema-completo]] | [[10-base-de-datos/10-03-backup]] | [[04-tpv-hosteleria/04-01-vision-general]] | [[05-tpv-comercio/05-01-vision-general]]
Estado: #pendiente

---

## Flujo

```
[Pulsar "Cierre de caja"]
  ↓
Comprobar comandas abiertas (hostelería) → avisar si hay
  ↓
Mostrar resumen del día:
  - Nº tickets, total efectivo, total tarjeta, total general
  - Desglose IVA por tipo
  ↓
Campo "Efectivo real contado"
  → Descuadre = real - esperado (verde si sobrante, rojo si faltante)
  ↓
[Cerrar e imprimir] / [Cerrar sin imprimir]
  ↓
Guardar en tabla caja → BackupManager.realizarBackupDiario() → [[10-base-de-datos/10-03-backup]]
```

## Filtrado por Módulo

- Desde hostelería → solo facturas `tipo_negocio = HOSTELERIA`
- Desde comercio → solo facturas `tipo_negocio = COMERCIO`

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[10-base-de-datos/10-01-esquema-completo]] y este documento.

Genera:
1. pantalla-cierre-caja.fxml
   - Tabla resumen del día (ventas, formas de pago)
   - Tabla desglose IVA
   - Campo "Efectivo real contado" con descuadre en tiempo real
   - Botones "Cerrar e imprimir" / "Cerrar sin imprimir" / "Cancelar"

2. CierreCajaController.java en com.tpvfacil.core.ui
   - Filtra facturas por TipoNegocio según desde dónde se llame
   - No permite doble cierre del mismo día
   - Al cerrar: guardar en CajaRepository + BackupManager.realizarBackupDiario()
   - Confirmar antes de cerrar (diálogo)
```
