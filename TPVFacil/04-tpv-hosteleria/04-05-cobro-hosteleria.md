# 04-05 — Cobro en Hostelería

#hosteleria #implementacion
Relacionado: [[CLAUDE.md]] | [[04-03-comandas]] | [[02-verifactu/02-02-flujo-tecnico]] | [[06-licencias/06-01-modo-demo]]
Estado: #pendiente

---

## Flujo

```
[Cobrar desde Comanda]
  ↓
Pantalla de cobro:
  - Resumen líneas de la comanda
  - Desglose IVA
  - ☐ "El cliente quiere factura completa" (campo NIF si se marca)
  ↓
Selector forma de pago:
  [💵 Efectivo] [💳 Tarjeta] [🔀 Mixto]
  → Efectivo: teclado numérico + cambio en tiempo real
  ↓
[✅ Confirmar cobro]
  ↓
ModoDemo.puedeEmitirTicket() → [[06-licencias/06-01-modo-demo]]
  ↓ sí
VerifactuManager.procesarFactura() → [[02-verifactu/02-02-flujo-tecnico]]
  ↓
Comanda → PAGADA | Mesa → LIBRE
  ↓
Imprimir ticket → [[03-app-core/03-07-impresion-tickets]]
```

## División de Cuenta

Modal simple: introducir nº personas → muestra importe por persona.
Versión 1.0: solo división equitativa (total / personas).

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[02-verifactu/02-02-flujo-tecnico]], [[06-licencias/06-01-modo-demo]] y este documento.

Genera:
1. pantalla-cobro-hosteleria.fxml
   Panel izquierdo: resumen + IVA. Panel derecho: forma pago + teclado + confirmar.
   Modal de división de cuenta accesible desde botón "Dividir"

2. CobroHosteleriaController.java en com.tpvfacil.hosteleria.controller
   Comprobar ModoDemo antes de procesar.
   Al confirmar: VerifactuManager.procesarFactura(), actualizar comanda y mesa en BD.
   Si Verifactu falla: completar venta de todas formas (PENDIENTE).
```
