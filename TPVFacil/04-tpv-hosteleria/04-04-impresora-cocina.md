# 04-04 — Impresora de Cocina

#hosteleria #implementacion
Relacionado: [[CLAUDE.md]] | [[04-03-comandas]] | [[03-app-core/03-07-impresion-tickets]] | [[03-app-core/03-05-configuracion-negocio]]
Estado: #pendiente

---

## Formato del Ticket de Cocina

```
════════════════
     COCINA
════════════════
Mesa: 5 — Sala
Hora: 14:45
════════════════
x2  Café con leche
x1  Tostada con tomate
     ⚠ Sin gluten
x1  Agua con gas
════════════════
```

Sin precios. Solo líneas nuevas (no ya enviadas). Observaciones con ⚠.

## Configuración

En [[03-app-core/03-05-configuracion-negocio]] → tab Impresión → selector "Impresora de cocina".

Si no configurada: Snackbar de aviso pero la operación continúa (comanda se marca enviada).

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[03-app-core/03-07-impresion-tickets]] y este documento.

En TicketPrinter.java añade:
  void imprimirComanda(Comanda comanda, List<LineaComanda> lineasNuevas)
  - Si no hay impresora cocina configurada: log warning, no lanzar excepción
  - Formato exacto como se muestra arriba
  - Observaciones de línea: en línea siguiente con "   ⚠ "
  - Imprime en la impresora configurada como "impresora_cocina"
```
