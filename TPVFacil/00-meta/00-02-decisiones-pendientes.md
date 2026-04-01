# 00-02 — Decisiones Pendientes

#meta #pendiente #bloqueante
Relacionado: [[CLAUDE.md]] | [[00-01-glosario]] | [[11-fases/11-01-roadmap]]

> Marcar con ✅ cuando esté decidido. Actualizar el documento afectado.

---

## 🔴 Críticas — Bloquean Desarrollo

### ⬜ Nombre definitivo del producto
- **Placeholder actual:** TPVFácil
- **Afecta a:** [[08-web/08-01-estructura-web]], instalador, paquete Java `com.tpvfacil`
- **Decidir antes de:** [[11-fases/11-07-fase6-web|Fase 6]]
- **Acción:** Verificar dominio disponible (.es y .com), comprobar marca

---

### ⬜ Sistema de activación y venta
- **Afecta a:** [[06-licencias/06-02-activacion-clave]], [[08-web/08-05-pagina-precios]]
- **Decidir antes de:** [[11-fases/11-07-fase6-web|Fase 6]]
- Ver opciones detalladas en [[06-licencias/06-03-opciones-plataforma-pago]]

| Opción | Pros | Contras |
|--------|------|---------|
| Manual | Cero coste | Laborioso |
| Gumroad | Automático, gratis | Comisión ~10% |
| Paddle | Automático, IVA EU gestionado | Comisión ~5% + 0,50€ |
| Stripe + servidor | Control total | Requiere backend |

> **Recomendación para empezar:** Gumroad o Paddle.

---

### ⬜ Precio exacto
- **Placeholder:** máximo 100 €
- **Decidir antes de:** [[11-fases/11-07-fase6-web|Fase 6]]
- ¿Un precio para ambos módulos? ¿Diferente para hostelería y comercio?
- ¿Precio de lanzamiento especial? Ver [[11-fases/11-09-fase8-lanzamiento]]

---

## 🟡 Secundarias — No Bloquean

### ⬜ Firma del ejecutable .exe
- **Afecta a:** [[07-instalador/07-02-firma-ejecutable]]
- **Decidir antes de:** [[11-fases/11-06-fase5-instalador|Fase 5]]
- Sin firma → alerta SmartScreen de Windows (solventable con instrucciones al usuario)
- Con firma OV (~150€/año) → sin alerta

### ⬜ Soporte post-venta
- ¿Email? ¿Chat? ¿Sin soporte?
- Decidir antes de [[11-fases/11-09-fase8-lanzamiento|Fase 8]]

### ⬜ Política de actualizaciones
- ¿Gratis siempre? ¿Solo 12 meses? ¿Pago por versión mayor?
- Afecta al copy de [[08-web/08-05-pagina-precios]]
