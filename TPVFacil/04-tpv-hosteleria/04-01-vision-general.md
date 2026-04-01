# 04-01 — TPV Hostelería: Visión General

#hosteleria
Relacionado: [[CLAUDE.md]] | [[03-app-core/03-01-main-java]] | [[02-verifactu/02-02-flujo-tecnico]] | [[11-fases/11-05-fase4-hosteleria]]
Estado: #pendiente

---

## Para Quién

Bares, restaurantes, cafeterías, terrazas. El cliente se sienta → el dependiente toma el pedido → se cobra al final.

## Flujo Principal

```
Pantalla de Mesas → [[04-02-gestion-mesas]]
  ↓ clic en mesa libre
Pantalla de Comanda → [[04-03-comandas]]
  ↓ añadir productos de la carta → [[04-06-carta-menu]]
  ↓ "Enviar a cocina" → [[04-04-impresora-cocina]]
  ↓ "Cobrar"
Pantalla de Cobro → [[04-05-cobro-hosteleria]]
  ↓ confirmar
Verifactu → [[02-verifactu/02-02-flujo-tecnico]]
  ↓
Ticket impreso + Mesa → LIBRE
```

## Estados de Mesa

| Estado | Color | Acción al clicar |
|--------|-------|-----------------|
| LIBRE | 🟢 `#27AE60` | Abre comanda nueva |
| OCUPADA | 🔴 `#E74C3C` | Abre comanda activa |
| PENDIENTE_PAGO | 🟠 `#F39C12` | Va directo al cobro |

## Documentos del Módulo

- [[04-02-gestion-mesas]] — Vista de mesas, CRUD
- [[04-03-comandas]] — Pantalla de pedido
- [[04-04-impresora-cocina]] — Envío a cocina
- [[04-05-cobro-hosteleria]] — Proceso de cobro
- [[04-06-carta-menu]] — Gestión de productos

## Prompt de Inicio para Claude

```
Lee [[CLAUDE.md]], este documento y TODOS los documentos en 04-tpv-hosteleria/.
Prerequisitos completados: [[11-fases/11-02-fase1-nucleo|Fase 1]] y [[11-fases/11-03-fase2-verifactu|Fase 2]].
Empezar por [[04-02-gestion-mesas]].
```
