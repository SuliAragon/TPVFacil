# 07-02 — Firma del Ejecutable

#instalador
Relacionado: [[CLAUDE.md]] | [[07-01-jpackage]] | [[00-meta/00-02-decisiones-pendientes]]
Estado: #decision-pendiente

---

## El Problema

Sin firma, Windows SmartScreen muestra alerta al ejecutar el instalador.

## Opciones

| Opción | Coste | Resultado |
|--------|-------|-----------|
| Sin firma (v1.0) | 0€ | Alerta SmartScreen → usuario pulsa "Más información" → "Ejecutar" |
| Certificado OV | ~150€/año | Elimina alerta para la mayoría |
| Certificado EV | ~400€/año | Elimina TODAS las alertas desde el primer día |

## Estrategia Recomendada

- **v1.0:** Sin firma. Instrucciones claras en [[08-web/08-06-pagina-descarga]].
- **Con primeros ingresos:** Comprar certificado OV (Sectigo, DigiCert).

## Firma con Certificado (cuando se tenga)

```bash
signtool sign /f cert.pfx /p password \
  /tr http://timestamp.digicert.com \
  /fd sha256 /td sha256 \
  "TPVFacil-Setup-1.0.0.exe"
```
