# 00-03 — Árbol de Dependencias

#meta #referencia #orden-lectura
Relacionado: [[CLAUDE.md]] | [[11-fases/11-01-roadmap]]

> Usa este documento para saber qué leer antes de implementar cada módulo.

---

## Orden de Lectura por Fase

### Para Fase 1 — Núcleo
```
[[CLAUDE.md]]
[[00-meta/00-01-glosario]]
[[01-arquitectura/01-01-estructura-proyecto]]
[[01-arquitectura/01-02-stack-tecnologico]]
[[01-arquitectura/01-03-estructura-paquetes-java]]
[[01-arquitectura/01-04-configuracion-maven]]
[[10-base-de-datos/10-01-esquema-completo]]
[[10-base-de-datos/10-02-migraciones]]
[[03-app-core/03-02-base-de-datos-sqlite]]
[[03-app-core/03-03-modelos-datos]]
[[03-app-core/03-01-main-java]]
[[03-app-core/03-05-configuracion-negocio]]
[[06-licencias/06-01-modo-demo]]
[[06-licencias/06-02-activacion-clave]]
[[06-licencias/06-04-hardware-fingerprint]]
```

### Para Fase 2 — Verifactu
```
[[02-verifactu/02-01-que-es-verifactu]]
[[02-verifactu/02-02-flujo-tecnico]]
[[02-verifactu/02-03-clases-java]]
[[02-verifactu/02-04-xml-schema]]
[[02-verifactu/02-05-api-aeat]]
[[02-verifactu/02-06-certificados]]
[[02-verifactu/02-07-qr-ticket]]
[[03-app-core/03-07-impresion-tickets]]
```

### Para Fase 3 — TPV Comercio
```
[[05-tpv-comercio/05-01-vision-general]]
[[05-tpv-comercio/05-02-pantalla-venta]]
[[05-tpv-comercio/05-03-lector-codigo-barras]]
[[05-tpv-comercio/05-04-gestion-stock]]
[[05-tpv-comercio/05-05-devoluciones]]
[[05-tpv-comercio/05-06-cobro-comercio]]
[[03-app-core/03-06-cierre-caja]]
[[10-base-de-datos/10-03-backup]]
```

### Para Fase 4 — TPV Hostelería
```
[[04-tpv-hosteleria/04-01-vision-general]]
[[04-tpv-hosteleria/04-02-gestion-mesas]]
[[04-tpv-hosteleria/04-03-comandas]]
[[04-tpv-hosteleria/04-04-impresora-cocina]]
[[04-tpv-hosteleria/04-05-cobro-hosteleria]]
[[04-tpv-hosteleria/04-06-carta-menu]]
```

### Para Fase 5 — Instalador
```
[[07-instalador/07-01-jpackage]]
[[07-instalador/07-02-firma-ejecutable]]
[[07-instalador/07-03-proceso-build]]
```

### Para Fase 6 — Web
```
[[08-web/08-01-estructura-web]]
[[09-seo/09-04-seo-tecnico]]
[[09-seo/09-02-palabras-clave]]
[[08-web/08-02-landing-page]]
[[08-web/08-03-pagina-hosteleria]]
[[08-web/08-04-pagina-comercio]]
[[08-web/08-05-pagina-precios]]
[[08-web/08-06-pagina-descarga]]
[[09-seo/09-01-estrategia-seo]]
[[09-seo/09-03-blog-articulos]]
```

---

## Grafo de Dependencias

```
[[03-app-core/03-01-main-java|Core]] ──────────────────────────┐
                                                                ▼
[[02-verifactu/02-02-flujo-tecnico|Verifactu]] ────┬──► [[04-tpv-hosteleria/04-01-vision-general|Hostelería]]
                                                   │
                                                   └──► [[05-tpv-comercio/05-01-vision-general|Comercio]]

[[06-licencias/06-02-activacion-clave|Licencias]] ──────────────────────► Core (arranque)
[[10-base-de-datos/10-01-esquema-completo|Base de datos]] ────────────────► Core

[[07-instalador/07-01-jpackage|Instalador]] ◄── (todo lo anterior completo)
[[08-web/08-01-estructura-web|Web]] + [[09-seo/09-01-estrategia-seo|SEO]] ──── (paralelo, independiente)
```
