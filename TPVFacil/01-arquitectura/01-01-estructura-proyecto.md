# 01-01 — Estructura del Proyecto

#arquitectura #referencia
Relacionado: [[CLAUDE.md]] | [[01-02-stack-tecnologico]] | [[01-03-estructura-paquetes-java]] | [[01-04-configuracion-maven]]

---

## Árbol de Carpetas

```
/TPVFacil
├── CLAUDE.md                    ← LEER SIEMPRE PRIMERO
├── README.md
│
├── /app                         ← Aplicación Java
│   ├── pom.xml                  → ver [[01-04-configuracion-maven]]
│   ├── build.sh                 → ver [[07-instalador/07-03-proceso-build]]
│   └── /src/main
│       ├── /java/com/tpvfacil   → ver [[01-03-estructura-paquetes-java]]
│       └── /resources
│           ├── /fxml
│           ├── /css
│           ├── /images
│           └── config.properties
│
└── /web                         ← Sitio web
    ├── /public
    │   ├── index.html           → [[08-web/08-02-landing-page]]
    │   ├── /hosteleria          → [[08-web/08-03-pagina-hosteleria]]
    │   ├── /comercio            → [[08-web/08-04-pagina-comercio]]
    │   ├── /precios             → [[08-web/08-05-pagina-precios]]
    │   ├── /descargar           → [[08-web/08-06-pagina-descarga]]
    │   ├── /verifactu
    │   ├── /blog
    │   └── /legal
    ├── /assets/{css,js,images}
    ├── sitemap.xml
    └── robots.txt
```

---

## Regla de Oro

> `/app` y `/web` son mundos **completamente separados**.
> Ningún archivo de uno referencia al otro.

---

## Archivos Clave

| Archivo | Propósito | Doc |
|---------|-----------|-----|
| `app/pom.xml` | Dependencias Maven | [[01-04-configuracion-maven]] |
| `app/.../Main.java` | Punto de entrada | [[03-app-core/03-01-main-java]] |
| `app/.../config.properties` | Config por defecto | [[03-app-core/03-05-configuracion-negocio]] |
| `web/public/index.html` | Landing page | [[08-web/08-02-landing-page]] |
| `web/sitemap.xml` | SEO | [[09-seo/09-04-seo-tecnico]] |
