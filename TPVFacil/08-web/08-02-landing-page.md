# 08-02 — Landing Page (index.html)

#web #seo
Relacionado: [[CLAUDE.md]] | [[08-01-estructura-web]] | [[09-seo/09-02-palabras-clave]] | [[09-seo/09-04-seo-tecnico]]
Estado: #pendiente

---

## SEO

```
Title:       "TPVFácil — Software TPV con Verifactu para Hostelería y Comercio | 79€"
Description: "Software TPV con Verifactu incluido para bares, restaurantes y tiendas.
              Pago único desde 79€. Sin suscripciones. Descarga gratis la demo."
H1:          "Software TPV con Verifactu incluido"
```

## Secciones (en orden)

1. **Hero** — headline + precio + CTA descarga + imagen del programa
2. **Dolor→Solución** — TPV tradicional 30-80€/mes vs TPVFácil 79€ único
3. **Dos módulos** — [[08-03-pagina-hosteleria|Hostelería]] y [[08-04-pagina-comercio|Comercio]] en tarjetas side-by-side
4. **Verifactu incluido** — explicación simple + enlace a `/verifactu`
5. **Características** — 6 iconos con ventajas clave
6. **Precios** — Demo gratis vs Licencia 79€ → [[08-05-pagina-precios]]
7. **FAQ** — Schema.org FAQPage (mínimo 6 preguntas)
8. **CTA final** — "Prueba gratis hoy mismo" + botón

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[08-01-estructura-web]], este documento, [[09-seo/09-02-palabras-clave]] y [[09-seo/09-04-seo-tecnico]].

Genera /web/public/index.html completo con:
- Todas las secciones en el orden indicado
- Schema.org SoftwareApplication + FAQPage en JSON-LD
- Meta title, description, OG tags como se especifica
- Botón "Descargar demo" → /descargar | Botón "Ver precios" → /precios
- FAQ con mínimo 6 preguntas relevantes para un negocio español
- Responsive, mobile-first, tiempo de carga optimizado
```
