# 11-07 — Fase 6: Web

#fase #fase-6
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[08-web/08-01-estructura-web]] | [[09-seo/09-01-estrategia-seo]]
Prerequisito: [[11-06-fase5-instalador|Fase 5 ✅]] | [[00-meta/00-02-decisiones-pendientes|Decisiones ✅]]
Estado: #pendiente

---

## Criterio de Éxito

- ✅ Web publicada en dominio elegido con HTTPS
- ✅ Google Search Console: páginas indexadas sin errores
- ✅ PageSpeed Insights > 90 en móvil y escritorio
- ✅ Schema.org válido (Rich Results Test)
- ✅ Botón de compra redirige a plataforma de pago real
- ✅ Botón de descarga apunta al instalador real

---

## Prerequisitos Antes de Empezar

- Decisiones tomadas en [[00-meta/00-02-decisiones-pendientes]]:
  - ✅ Nombre definitivo → dominio comprado
  - ✅ Plataforma de pago elegida → [[06-licencias/06-03-opciones-plataforma-pago]]
  - ✅ Precio exacto definido

---

## Tareas

### 6.1 — Archivos base
```
Lee [[CLAUDE.md]], [[08-web/08-01-estructura-web]] y [[09-seo/09-04-seo-tecnico]].
Genera: /web/assets/css/global.css + /web/assets/js/main.js + robots.txt + sitemap.xml
```

### 6.2 — Landing page
```
Lee [[CLAUDE.md]], [[08-web/08-02-landing-page]], [[09-seo/09-02-palabras-clave]] y [[09-seo/09-04-seo-tecnico]].
Genera /web/public/index.html completo.
```

### 6.3 — Páginas de módulos
```
Lee [[CLAUDE.md]], [[08-web/08-03-pagina-hosteleria]] y [[08-web/08-04-pagina-comercio]].
Genera /hosteleria/index.html y /comercio/index.html.
```

### 6.4 — Páginas de conversión
```
Lee [[CLAUDE.md]], [[08-web/08-05-pagina-precios]] y [[08-web/08-06-pagina-descarga]].
Genera /precios/index.html y /descargar/index.html.
URL de descarga → enlace real a GitHub Releases.
URL de compra → plataforma de pago elegida.
```

### 6.5 — Página Verifactu + Blog
```
Lee [[CLAUDE.md]], [[09-seo/09-01-estrategia-seo]] y [[09-seo/09-03-blog-articulos]].
Genera /verifactu/index.html (página estática informativa).
Genera /blog/index.html + /blog/que-es-verifactu/index.html.
```

### 6.6 — Legales
```
Genera /legal/aviso-legal.html, /legal/privacidad.html, /legal/cookies.html.
Banner de cookies en main.js (aparece primera visita).
```

### 6.7 — Publicar y configurar
```
1. Subir /web/ a GitHub Pages / Netlify / Vercel
2. Configurar dominio + HTTPS
3. Dar de alta en Google Search Console
4. Enviar sitemap.xml
5. Configurar Google Analytics / Plausible
```

---

## Verificación

```
Herramientas (gratuitas):
- PageSpeed: https://pagespeed.web.dev/ → > 90 en móvil
- Rich Results: https://search.google.com/test/rich-results → Schema.org válido
- Mobile-Friendly: https://search.google.com/test/mobile-friendly
- Validador W3C: https://validator.w3.org/
```
