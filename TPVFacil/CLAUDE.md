# CLAUDE.md — Prompt Maestro del Proyecto TPVFácil

> **🧠 Este es el cerebro del proyecto.** Léelo completo antes de generar cualquier código.
> En Obsidian: abre `TPVFacil-Mapa-Proyecto.canvas` para ver el grafo visual completo.

#meta #prompt-maestro

---

## 🎯 Qué Estamos Construyendo

Un programa **descargable para Windows, macOS y Linux** que:
1. Permite a negocios españoles gestionar ventas desde su propio ordenador
2. Cumple con [[02-verifactu/02-01-que-es-verifactu|Verifactu (AEAT)]] desde el primer día
3. Tiene **dos módulos**: [[04-tpv-hosteleria/04-01-vision-general|TPV Hostelería]] y [[05-tpv-comercio/05-01-vision-general|TPV Comercio]]
4. Funciona en [[06-licencias/06-01-modo-demo|modo demo]] gratis y [[06-licencias/06-02-activacion-clave|modo completo]] con clave
5. Se vende por **150 €** pago único — sin suscripciones

Además: una [[08-web/08-01-estructura-web|web corporativa]] para vender el software y posicionarse en Google ([[09-seo/09-01-estrategia-seo|estrategia SEO]]).

---

## 🏗 Reglas de Arquitectura — NUNCA Romper

```
/TPVFacil
  /app    ← Todo Java. NUNCA mezclar con /web
  /web    ← Todo HTML/CSS/JS. NUNCA mezclar con /app
```

Ver estructura completa en [[01-arquitectura/01-01-estructura-proyecto]].

---

## ⚙️ Stack Tecnológico

Ver justificación completa en [[01-arquitectura/01-02-stack-tecnologico]].

| Parte | Tecnología |
|-------|-----------|
| App escritorio | Java 21 + JavaFX 21 |
| Base de datos | SQLite (sin servidor) |
| Build | Maven + jpackage (instaladores .exe / .dmg / .deb/.rpm con JRE incluido) |
| Web | HTML/CSS/JS puro (sin frameworks) |

Ver [[01-arquitectura/01-04-configuracion-maven|configuración Maven]] y [[01-arquitectura/01-03-estructura-paquetes-java|estructura de paquetes]].

---

## 🗂 Mapa de la Documentación

```
CLAUDE.md  ← SIEMPRE PRIMERO
│
├── [[00-meta/00-01-glosario]]              Términos del proyecto
├── [[00-meta/00-02-decisiones-pendientes]] ⚠ Decisiones sin tomar
├── [[00-meta/00-03-arbol-dependencias]]    Orden de lectura
│
├── [[01-arquitectura/01-01-estructura-proyecto]]
├── [[10-base-de-datos/10-01-esquema-completo]]
├── [[03-app-core/03-01-main-java]]
│
├── [[02-verifactu/02-02-flujo-tecnico]]    ← Core legal
│
├── [[04-tpv-hosteleria/04-01-vision-general]]
├── [[05-tpv-comercio/05-01-vision-general]]
│
├── [[06-licencias/06-01-modo-demo]]
├── [[07-instalador/07-01-jpackage]]
│
├── [[08-web/08-01-estructura-web]]
├── [[09-seo/09-01-estrategia-seo]]
│
└── [[11-fases/11-01-roadmap]]              ← Hoja de ruta
```

---

## 🔐 Sistema de Licencias

- **Demo:** activo por defecto. Máximo 20 tickets/día. Verifactu en sandbox. Ver [[06-licencias/06-01-modo-demo]].
- **Completo:** se desbloquea con clave. Sin límites. Verifactu en producción. Ver [[06-licencias/06-02-activacion-clave]].
- La clave se vincula al hardware del equipo. Ver [[06-licencias/06-04-hardware-fingerprint]].

---

## ⚡ Verifactu — Resumen

Cada factura → hash SHA-256 encadenado → firma digital → envío AEAT → QR en ticket.
Ver flujo completo en [[02-verifactu/02-02-flujo-tecnico]].

---

## 🤖 Instrucciones para Claude (Agente)

1. **Leer siempre este CLAUDE.md primero**
2. Leer el documento específico del módulo a implementar
3. Respetar [[01-arquitectura/01-03-estructura-paquetes-java|la estructura de paquetes]]
4. Interfaz en **español**, código en **inglés**
5. Javadoc en español en métodos públicos
6. Nunca hardcodear credenciales — todo a [[03-app-core/03-05-configuracion-negocio|ConfiguracionManager]]
7. [[02-verifactu/02-02-flujo-tecnico|Verifactu]] en sandbox si demo, producción si completo
8. Manejar siempre excepciones — nunca `catch` vacío

---

## 📍 Estado del Proyecto

> Última actualización: 2026-04-01
> Progreso detallado → [[00-meta/00-04-progreso]]

| Fase | Estado | Fecha |
|------|--------|-------|
| [[11-fases/11-02-fase1-nucleo\|Fase 1 — Núcleo]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-03-fase2-verifactu\|Fase 2 — Verifactu]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-04-fase3-comercio\|Fase 3 — Comercio]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-05-fase4-hosteleria\|Fase 4 — Hostelería]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-06-fase5-instalador\|Fase 5 — Instalador]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-07-fase6-web\|Fase 6 — Web]] | ✅ Completada | 2026-04-01 |
| [[11-fases/11-08-fase7-beta\|Fase 7 — Beta]] | ⬜ Pendiente | — |
| [[11-fases/11-09-fase8-lanzamiento\|Fase 8 — Lanzamiento]] | ⬜ Pendiente | — |

## 🖥 Entorno de Desarrollo (Mac)

```bash
# Ejecutar app
cd App
JAVA_HOME=/opt/homebrew/opt/openjdk mvn javafx:run

# Java disponible en el Mac: Java 25 (Homebrew)
# Target de compilación: Java 21 (--release 21)
# BD creada en: ~/Library/Application Support/TPVFacil/tpvfacil.db
```
