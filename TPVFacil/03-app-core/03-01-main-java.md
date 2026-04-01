# 03-01 — Main.java y Arranque

#app-core #fase-1
Relacionado: [[CLAUDE.md]] | [[01-arquitectura/01-03-estructura-paquetes-java]] | [[11-fases/11-02-fase1-nucleo]]
Estado: #pendiente

---

## Flujo de Arranque

```
Main.java (extends Application)
│
1. DatabaseManager.inicializar()     → [[10-base-de-datos/10-02-migraciones]]
2. ConfiguracionManager.cargar()     → [[03-05-configuracion-negocio]]
3. LicenciaManager.comprobar()       → [[06-licencias/06-02-activacion-clave]]
4. VerifactuManager.reenviarPendientes() → [[02-verifactu/02-02-flujo-tecnico]]
5. isConfigurado() ?
   └── NO → Wizard primera ejecución → [[03-05-configuracion-negocio]]
   └── SÍ → Pantalla de inicio (inicio.fxml)
```

## Título de la Ventana

- MODO DEMO → `"TPVFácil [DEMO]"`
- MODO COMPLETO → `"TPVFácil"`

## Banner DEMO

Label fijo en la parte inferior (visible en todas las pantallas):
```
⚠ MODO DEMO — Tickets hoy: 14/20 — [Activar licencia →]
```
Color: fondo `#F39C12` (naranja), texto negro.
Ver lógica del límite en [[06-licencias/06-01-modo-demo]].

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera:
1. Main.java — extiende Application, flujo de arranque completo
   Ventana mínima 1024x768, redimensionable
   Icono desde /resources/images/logo.png

2. inicio.fxml + InicioController.java
   Fondo: #1B4F8A (azul oscuro)
   Dos botones grandes: "🍽 Hostelería" y "🛍 Comercio"
   Cada botón: 200x120px, fondo blanco, texto azul, esquinas redondeadas
   Banner DEMO visible si LicenciaManager.isModoDemo()
   Botón "Activar licencia" en el banner que navega a [[06-licencias/06-02-activacion-clave]]

3. Wizard primera ejecución → ver [[03-05-configuracion-negocio]]
```
