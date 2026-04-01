# 05-01 — TPV Comercio: Visión General

#comercio
Relacionado: [[CLAUDE.md]] | [[03-app-core/03-01-main-java]] | [[02-verifactu/02-02-flujo-tecnico]] | [[11-fases/11-04-fase3-comercio]]
Estado: #pendiente

---

## Para Quién

Tiendas, comercios, papelerías, farmacias... El dependiente atiende en mostrador, escanea o busca el producto, y cobra inmediatamente.

## Flujo Principal

```
Pantalla de Venta → [[05-02-pantalla-venta]]
  ↓ buscar / escanear código de barras → [[05-03-lector-codigo-barras]]
  ↓ añadir a cesta
  ↓ "Cobrar"
Pantalla de Cobro → [[05-06-cobro-comercio]]
  ↓ confirmar
Verifactu → [[02-verifactu/02-02-flujo-tecnico]]
  ↓
Ticket impreso + Stock descontado → [[05-04-gestion-stock]]
```

## Documentos del Módulo

- [[05-02-pantalla-venta]] — Pantalla principal con catálogo y cesta
- [[05-03-lector-codigo-barras]] — Integración lector USB
- [[05-04-gestion-stock]] — Control de inventario
- [[05-05-devoluciones]] — Proceso de devolución
- [[05-06-cobro-comercio]] — Proceso de cobro

## Diferencias Visuales con Hostelería

| | Hostelería | Comercio |
|---|---|---|
| Fondo | Oscuro #1A1A2E | Claro #F4F6F9 |
| Concepto UI | Mesas → comandas | Cesta de compra |
| IVA default | 10% | 21% (configurable) |

## Prompt de Inicio para Claude

```
Lee [[CLAUDE.md]], este documento y TODOS los documentos en 05-tpv-comercio/.
Prerequisitos: [[11-fases/11-02-fase1-nucleo|Fase 1]] y [[11-fases/11-03-fase2-verifactu|Fase 2]].
Empezar por [[05-02-pantalla-venta]].
```
