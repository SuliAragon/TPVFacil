# 08-01 — Estructura Web

#web #referencia
Relacionado: [[CLAUDE.md]] | [[09-seo/09-04-seo-tecnico]] | [[11-fases/11-07-fase6-web]]
Estado: #pendiente

---

## Principios

1. **HTML/CSS/JS puro** — sin frameworks
2. **Mobile-first** — primero móvil, luego escritorio
3. **SEO-first** — cada página optimizada → [[09-seo/09-01-estrategia-seo]]
4. **Conversión** — cada página lleva al visitante a descargar o comprar

## Paleta de Colores

```css
:root {
  --azul:     #1B4F8A;
  --verde:    #27AE60;  /* CTAs principales */
  --naranja:  #E67E22;  /* Acentos */
  --fondo:    #F8F9FA;
  --texto:    #2C3E50;
  --gris:     #7F8C8D;
}
```

## Tipografía

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

## Componentes CSS Globales (global.css)

```
.btn-primario    → verde, grande, CTA principal
.btn-secundario  → azul outline
.tarjeta         → card con sombra suave
.seccion         → padding vertical generoso
.contenedor      → max-width 1200px centrado
nav              → sticky, sombra al hacer scroll
footer           → pie de página estándar
```

## Nav (todas las páginas)

```
Logo | Hostelería | Comercio | Precios | Blog | [Descargar gratis →]
```

## Páginas y sus Documentos

| Página | Documento |
|--------|-----------|
| `/` | [[08-02-landing-page]] |
| `/hosteleria` | [[08-03-pagina-hosteleria]] |
| `/comercio` | [[08-04-pagina-comercio]] |
| `/precios` | [[08-05-pagina-precios]] |
| `/descargar` | [[08-06-pagina-descarga]] |
| `/verifactu` | (artículo informativo) |
| `/blog` | → [[09-seo/09-03-blog-articulos]] |

## Prompt Base para Claude (Web)

```
Lee [[CLAUDE.md]], este documento y [[09-seo/09-04-seo-tecnico]].

Para CADA página web:
1. HTML5 semántico (header, main, section, footer)
2. Importar global.css. CSS específico en <style> al inicio del <head>.
3. Schema.org JSON-LD apropiado
4. Meta title, description, canonical, OG tags únicos
5. Imágenes con alt descriptivo. Formato .webp preferido.
6. Un solo H1 por página.
7. Mobile-first y responsive.
```
