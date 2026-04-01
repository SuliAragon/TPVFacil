# 05-05 — Devoluciones

#comercio #implementacion
Relacionado: [[CLAUDE.md]] | [[05-02-pantalla-venta]] | [[02-verifactu/02-02-flujo-tecnico]] | [[05-04-gestion-stock]]
Estado: #pendiente

---

## Flujo

```
[Botón "Devolución" en pantalla de venta]
  ↓
Modal: buscar factura por número
  ↓
Mostrar líneas con checkbox para seleccionar qué se devuelve
  ↓
Total a devolver = suma de líneas seleccionadas
Forma de devolución: [Efectivo] [Tarjeta]
  ↓
[Confirmar devolución]
  → Crear Factura con tipo "R1" (rectificativa), importes negativos
  → VerifactuManager.procesarFactura() → [[02-verifactu/02-02-flujo-tecnico]]
  → StockService.reponerStock() para líneas devueltas → [[05-04-gestion-stock]]
  → Imprimir ticket de devolución con encabezado "DEVOLUCIÓN"
```

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[02-verifactu/02-02-flujo-tecnico]] y este documento.

Genera pantalla-devolucion.fxml (modal 600x500) + DevolucionController.java:
- Buscador de factura + TableView con checkbox por línea
- Total en tiempo real al marcar/desmarcar
- Al confirmar: Factura tipo R1 con importes negativos
- Ticket de devolución: encabezado "DEVOLUCIÓN" + "Rectifica factura: A-000123"
```
