# 06-01 — Modo Demo

#licencias #fase-1
Relacionado: [[CLAUDE.md]] | [[06-02-activacion-clave]] | [[06-04-hardware-fingerprint]] | [[03-app-core/03-01-main-java]]
Estado: #pendiente

---

## Límites

| Restricción | Valor |
|-------------|-------|
| Tickets/día | 20 |
| Tiempo | Ilimitado |
| Módulos | Ambos |
| Verifactu | Solo sandbox → [[02-verifactu/02-05-api-aeat]] |
| Marca de agua | "DEMO — No válido fiscalmente" |

## Indicadores en la App

- Título ventana: `"TPVFácil [DEMO]"`
- Banner inferior: `⚠ MODO DEMO — Tickets hoy: 14/20 — [Activar licencia →]`
  Color: `#F39C12` naranja
- Al llegar a 20: banner rojo + botón "Cobrar" desactivado

## Lógica

```java
// ModoDemo.java
public static boolean puedeEmitirTicket() {
    if (!LicenciaManager.isModoDemo()) return true;
    int hoy = FacturaRepository.contarPorFecha(LocalDate.now());
    return hoy < MAX_TICKETS_DIA; // 20
}
```

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera ModoDemo.java con puedeEmitirTicket() y ticketsRestantesHoy().

Modifica Main.java para mostrar banner DEMO y actualizar contador tras cada venta.

En CobroComercioController y CobroHosteleriaController:
Comprobar ModoDemo.puedeEmitirTicket() ANTES de abrir pantalla de cobro.
Si no puede: Alert informativo + botón "Activar licencia" que navega a [[06-02-activacion-clave]].
```
