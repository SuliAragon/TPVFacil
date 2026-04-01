# 04-03 — Comandas

#hosteleria #implementacion
Relacionado: [[CLAUDE.md]] | [[04-02-gestion-mesas]] | [[04-04-impresora-cocina]] | [[04-05-cobro-hosteleria]] | [[04-06-carta-menu]]
Estado: #pendiente

---

## Ciclo de Vida

```
ABIERTA → (añadir/quitar productos) → CERRADA → (cobrar) → PAGADA
```

## Layout de la Pantalla

```
┌──────────────────────────────────────────────────────┐
│  Mesa 5 — Sala | Abierta: 14:32 | 2 comensales       │
├─────────────────────────────┬────────────────────────┤
│  COMANDA ACTUAL             │  CARTA                 │
│  2x Café c/leche   3,00€   │  [Bebidas] [Comida]    │
│    [+][-][🗑]               │  [Menú]   [Postres]    │
│  1x Tostada        1,80€ 🔒 │                        │
│                             │  ☕ Café       1,20€   │
│  ─────────────────────────  │  🥛 Café c/leche 1,50€ │
│  Total: 4,80 €              │  🍺 Caña       1,80€   │
├─────────────────────────────┴────────────────────────┤
│  [Enviar a cocina]  [Cobrar]  [Obs.]  [← Mesas]      │
└──────────────────────────────────────────────────────┘
```

🔒 = ya enviado a cocina, no se puede modificar

## Reglas de Negocio

- Un producto enviado a cocina queda bloqueado (no se puede quitar)
- Al añadir: si ya existe en la comanda → incrementar cantidad
- Precio = precio actual del producto (snapshot en LineaComanda)
- Se puede reabrir una comanda CERRADA mientras no esté PAGADA

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[04-01-vision-general]] y este documento.

Genera:
1. pantalla-comanda.fxml — SplitPane 55/45
   Panel izquierdo: ListView de líneas + botones +/-/🗑 + total
   Panel derecho: TabPane de categorías + GridPane de productos
   Líneas enviadas a cocina: grises con 🔒, sin botones de edición

2. ComandaController.java en com.tpvfacil.hosteleria.controller
   Al abrir: cargar comanda ABIERTA o crear nueva
   Al clicar producto: añadir o incrementar cantidad
   Botón "Enviar a cocina": filtrar líneas nuevas → [[04-04-impresora-cocina]]
   Botón "Cobrar": pasar a [[04-05-cobro-hosteleria]]

3. ComandaService.java
   crearComanda(Mesa), cerrarComanda(id), calcularTotal(Comanda)
```
