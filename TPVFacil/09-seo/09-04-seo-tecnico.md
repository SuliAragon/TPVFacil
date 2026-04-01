# 09-04 — SEO Técnico

#seo #referencia
Relacionado: [[CLAUDE.md]] | [[08-web/08-01-estructura-web]] | [[09-05-schema-org]]

---

## Meta Tags Obligatorios (cada página)

```html
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>[TÍTULO ÚNICO] | TPVFácil</title>
<meta name="description" content="[150-160 chars únicos]">
<link rel="canonical" href="https://tpvfacil.es/[ruta]">

<!-- Open Graph -->
<meta property="og:title" content="...">
<meta property="og:description" content="...">
<meta property="og:image" content="https://tpvfacil.es/assets/images/og-image.jpg">
<meta property="og:url" content="https://tpvfacil.es/[ruta]">
<meta property="og:type" content="website">
<meta property="og:locale" content="es_ES">
```

## Schema.org por Tipo de Página

| Página | Schema.org | Doc |
|--------|-----------|-----|
| Todas | `SoftwareApplication` | [[09-05-schema-org]] |
| index + /precios | `FAQPage` | [[09-05-schema-org]] |
| Páginas internas | `BreadcrumbList` | [[09-05-schema-org]] |
| /precios | `Offer` | [[09-05-schema-org]] |
| Blog | `Article` | [[09-03-blog-articulos]] |

## Rendimiento (Core Web Vitals)

```html
<!-- Preconectar a Google Fonts -->
<link rel="preconnect" href="https://fonts.googleapis.com">

<!-- CSS crítico inline, resto diferido -->
<link rel="stylesheet" href="/assets/css/global.css" media="print" onload="this.media='all'">

<!-- Imágenes lazy + dimensiones declaradas -->
<img src="captura.webp" loading="lazy" width="800" height="500" alt="...">
```

## URLs

```
✅ /hosteleria (sin barra final, sin tildes)
✅ /blog/que-es-verifactu (slug con guiones)
❌ /hostelería (tildes en URL)
❌ /page?id=3 (parámetros)
```

## Checklist Antes de Publicar

```
[ ] title único (< 60 chars) y description única (150-160 chars) en cada página
[ ] Un solo H1 por página
[ ] Imágenes con alt descriptivo
[ ] URLs sin tildes ni parámetros
[ ] HTTPS activo
[ ] sitemap.xml enviado a Google Search Console
[ ] robots.txt correcto
[ ] Core Web Vitals > 90 en PageSpeed Insights
```
