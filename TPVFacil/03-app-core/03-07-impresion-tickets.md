# 03-07 — Impresión de Tickets

#app-core
Relacionado: [[CLAUDE.md]] | [[02-verifactu/02-07-qr-ticket]] | [[04-tpv-hosteleria/04-04-impresora-cocina]] | [[03-05-configuracion-negocio]]
Estado: #pendiente

---

## Formato del Ticket

```
────────────────────────────
       NOMBRE NEGOCIO
  CIF: B12345678
  C/ Dirección, 1 — Ciudad
────────────────────────────
Ticket Nº: A-000123
Fecha: 15/01/2025  14:32

  Café con leche x2    3,00€
  Tostada              1,80€
────────────────────────────
  Base (10%):          4,36€
  IVA (10%):           0,44€
  TOTAL:               4,80€
  Efectivo:            5,00€
  Cambio:              0,20€
────────────────────────────
[QR CODE]
CSV: XXXX-XXXX-XXXX-XXXX
Verifica: sede.agenciatributaria.gob.es
════ DEMO — NO VÁLIDO FISCALMENTE ════
   (solo en [[06-licencias/06-01-modo-demo|modo demo]])
────────────────────────────
```

## Clases

- `TicketFormatter.java` — genera el texto formateado según ancho de papel
- `QrGenerator.java` — genera el QR → [[02-verifactu/02-07-qr-ticket]]
- `TicketPrinter.java` — envía a la impresora (javax.print)

## Ancho de Papel

Configurado en [[03-05-configuracion-negocio]] (`ancho_papel_mm`):
- 58mm → 32 caracteres por línea
- 80mm → 48 caracteres por línea

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[02-verifactu/02-07-qr-ticket]] y este documento.

Genera:
1. TicketFormatter.java — formato según ancho papel. Marca de agua si demo.
2. TicketPrinter.java
   - imprimirTicket(Factura, RegistroVerifactu): impresión genérica javax.print
   - imprimirComanda(Comanda, List<LineaComanda>): para cocina → [[04-tpv-hosteleria/04-04-impresora-cocina]]
   - listarImpresoras(): List<String> con nombres de impresoras instaladas
```
