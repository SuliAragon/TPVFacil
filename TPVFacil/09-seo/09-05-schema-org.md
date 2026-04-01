# 09-05 — Schema.org Markup

#seo #referencia
Relacionado: [[CLAUDE.md]] | [[09-04-seo-tecnico]] | [[08-web/08-01-estructura-web]]

---

## SoftwareApplication (todas las páginas del software)

```json
{
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  "name": "TPVFácil",
  "description": "Software TPV para hostelería y comercio con Verifactu incluido. Pago único.",
  "applicationCategory": "BusinessApplication",
  "operatingSystem": "Windows 10, Windows 11",
  "offers": {
    "@type": "Offer",
    "price": "79",
    "priceCurrency": "EUR",
    "availability": "https://schema.org/InStock"
  },
  "softwareVersion": "1.0.0",
  "downloadUrl": "https://tpvfacil.es/descargar",
  "fileSize": "120MB",
  "author": { "@type": "Organization", "name": "TPVFácil" }
}
```

## FAQPage (index.html y /precios)

```json
{
  "@context": "https://schema.org",
  "@type": "FAQPage",
  "mainEntity": [
    {
      "@type": "Question",
      "name": "¿Necesito instalar Java para usar TPVFácil?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "No. El instalador incluye todo lo necesario."
      }
    }
  ]
}
```

## BreadcrumbList (páginas internas)

```json
{
  "@context": "https://schema.org",
  "@type": "BreadcrumbList",
  "itemListElement": [
    { "@type": "ListItem", "position": 1, "name": "Inicio", "item": "https://tpvfacil.es" },
    { "@type": "ListItem", "position": 2, "name": "Hostelería", "item": "https://tpvfacil.es/hosteleria" }
  ]
}
```

## Article (artículos de blog → [[09-03-blog-articulos]])

```json
{
  "@context": "https://schema.org",
  "@type": "Article",
  "headline": "Qué es Verifactu y cómo afecta a tu negocio",
  "datePublished": "2025-01-15",
  "author": { "@type": "Organization", "name": "TPVFácil" },
  "publisher": { "@type": "Organization", "name": "TPVFácil",
    "logo": { "@type": "ImageObject", "url": "https://tpvfacil.es/assets/images/logo.png" }
  }
}
```

## Regla: Siempre en `<script type="application/ld+json">` en el `<head>`
