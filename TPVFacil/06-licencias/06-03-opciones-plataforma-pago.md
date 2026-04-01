# 06-03 — Opciones de Plataforma de Pago

#licencias #pendiente
Relacionado: [[CLAUDE.md]] | [[00-meta/00-02-decisiones-pendientes]] | [[08-web/08-05-pagina-precios]]
Estado: #decision-pendiente

---

> ⚠ **Pendiente de decisión.** Ver [[00-meta/00-02-decisiones-pendientes]].

## Opciones

| Opción | Coste | Automatización | Complejidad |
|--------|-------|---------------|-------------|
| Manual | 0€ | Ninguna (tú envías la clave a mano) | Mínima |
| Gumroad | ~10% comisión | Total | Baja |
| Paddle | ~5% + 0,50€ | Total + IVA EU | Baja |
| Stripe + servidor | ~1,5% | Total (con backend) | Alta |

**Recomendación para empezar:** Gumroad o Paddle.

## Preparación del Código (ya implementado)

La URL de compra se lee de `config.properties`:
```properties
web.url.compra=https://tpvfacil.gumroad.com/l/tpvfacil
```

En [[06-02-activacion-clave]] el botón "Ir a comprar" abre esta URL. Solo hay que actualizar la URL cuando se decida la plataforma.
