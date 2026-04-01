# 05-06 — Cobro en Comercio

#comercio #implementacion
Relacionado: [[CLAUDE.md]] | [[05-02-pantalla-venta]] | [[02-verifactu/02-02-flujo-tecnico]] | [[06-licencias/06-01-modo-demo]]
Estado: #pendiente

---

## Flujo

```
[VentaController envía List<ItemCesta>]
  ↓
Pantalla de cobro: resumen + desglose IVA + total
  ↓
☐ "Factura completa" → campo NIF → buscar/crear cliente
  ↓
Selector forma de pago:
  Efectivo → teclado numérico + botones billete + cambio en tiempo real
  Tarjeta  → solo confirmar
  Mixto    → campo "tarjeta" + resto efectivo + cambio
  ↓
[✅ CONFIRMAR COBRO]
  ↓
ModoDemo.puedeEmitirTicket() → [[06-licencias/06-01-modo-demo]]
  ↓ sí
VentaService.construirFactura(cesta, formaPago, cliente) → Factura
  ↓
VerifactuManager.procesarFactura(factura) → [[02-verifactu/02-02-flujo-tecnico]]
  ↓
StockService.descontarStock(factura) → [[05-04-gestion-stock]]
  ↓
Imprimir ticket → [[03-app-core/03-07-impresion-tickets]]
  ↓
Animación "¡Venta completada! Cambio: X,XX€" (3 segundos)
Limpiar cesta y volver a pantalla de venta
```

## Manejo de Error Verifactu

Si falla: la venta **se completa igualmente**, registro queda `PENDIENTE`.
Aviso discreto: `"⚠ Ticket emitido — Registro Verifactu pendiente"`.

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[02-verifactu/02-02-flujo-tecnico]], [[06-licencias/06-01-modo-demo]] y este documento.

Genera:
1. pantalla-cobro-comercio.fxml — panel izq (resumen+IVA) / panel der (pago+teclado)
   Teclado con dígitos + botones de billete: 5€, 10€, 20€, 50€
   Botón COBRAR: verde, prominente

2. CobroComercioController.java
3. VentaService.construirFactura(): calcula base+cuota IVA agrupando por tipo
```
