# 05-02 — Pantalla de Venta

#comercio #implementacion
Relacionado: [[CLAUDE.md]] | [[05-01-vision-general]] | [[05-03-lector-codigo-barras]] | [[05-06-cobro-comercio]]
Estado: #pendiente

---

## Layout

```
┌──────────────────────────────────────────────────────┐
│ [🔍 Buscar producto o escanear código de barras...] │
├─────────────────────────────┬────────────────────────┤
│  CATÁLOGO (60%)             │  CESTA (40%)           │
│                             │                        │
│  [Bebidas] [Alimentación]   │  2x Leche      2,38€  │
│  [Limpieza] [Otros]         │  1x Pan        1,29€  │
│                             │  ─────────────────── │
│  🥛 Leche entera  1,19€    │  TOTAL:        3,67€  │
│  🍞 Pan molde     1,29€    │                        │
│  🥤 Refresco      1,19€    │  [🗑 Limpiar]          │
│                             │  [↩ Devolución]        │
│                             │  [💳 COBRAR 3,67€]    │
└─────────────────────────────┴────────────────────────┘
```

## Comportamiento

- Búsqueda filtra en tiempo real (nombre contiene el texto)
- Lector código de barras → detecta entrada rápida → [[05-03-lector-codigo-barras]]
- Clic en producto → añadir 1 unidad a cesta (o incrementar si ya está)
- En la cesta: botones `+` y `-` por línea, `🗑` para eliminar línea
- Total se recalcula en tiempo real
- Catálogo se carga en hilo secundario (`Task`) para no bloquear UI

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[05-01-vision-general]], [[05-03-lector-codigo-barras]] y este documento.

Genera:
1. pantalla-venta.fxml — SplitPane 60/40, fondo claro #F4F6F9
   Tarjetas de producto: fondo blanco, sombra, precio en azul #1B4F8A
   Botón COBRAR: verde #27AE60, ancho completo del panel derecho

2. VentaController.java en com.tpvfacil.comercio.controller
   Carga catálogo con Task en hilo secundario
   Filtrado en tiempo real al escribir en el buscador
   Integrar lector código de barras → [[05-03-lector-codigo-barras]]
   Gestión de cesta: añadir/incrementar/decrementar/eliminar
   Botón Devolución: abre modal → [[05-05-devoluciones]]
   Botón Cobrar: navega a [[05-06-cobro-comercio]] pasando la cesta
```
