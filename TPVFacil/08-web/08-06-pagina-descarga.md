# 08-06 — Página de Descarga

#web #conversion
Relacionado: [[CLAUDE.md]] | [[08-01-estructura-web]] | [[07-instalador/07-02-firma-ejecutable]]
Estado: #pendiente

---

## SEO

```
URL:         /descargar
Title:       "Descargar TPVFácil — Software TPV gratis para Windows"
Description: "Descarga TPVFácil gratis para Windows. No necesitas instalar Java.
              Compatible con Windows 10 y 11."
H1:          "Descarga TPVFácil gratis"
```

## Secciones

1. **Botón de descarga grande** — verde, centrado, con versión y tamaño
2. **Requisitos del sistema** — Windows 10/11, 4GB RAM, 500MB disco
3. **Instrucciones paso a paso** — numeradas con emoji
4. **Aviso SmartScreen** — caja amarilla explicando la alerta → [[07-instalador/07-02-firma-ejecutable]]
5. **Pasos tras instalar** — wizard de primera configuración
6. **¿Ya tienes licencia?** — instrucciones para activar

## El Aviso SmartScreen es Clave

```html
<!-- Caja amarilla visible y clara -->
💡 ¿Por qué aparece "Windows protegió tu equipo"?
Windows Defender muestra esta alerta en software nuevo que todavía
no tiene suficientes descargas. El programa es completamente seguro.
Solución: haz clic en "Más información" → "Ejecutar de todas formas".
```

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[08-01-estructura-web]] y este documento.
Genera /web/public/descargar/index.html.
Botón de descarga → URL real del instalador (placeholder: /assets/downloads/TPVFacil-Setup-1.0.0.exe).
Schema.org SoftwareApplication con downloadUrl y fileSize.
Evento analytics al hacer clic: gtag('event', 'download', {...}).
```
