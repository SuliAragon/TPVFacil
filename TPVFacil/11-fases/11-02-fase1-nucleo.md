# 11-02 — Fase 1: Núcleo

#fase #fase-1
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[01-arquitectura/01-04-configuracion-maven]] | [[10-base-de-datos/10-01-esquema-completo]] | [[06-licencias/06-02-activacion-clave]]
Estado: #pendiente

---

## Criterio de Éxito

- ✅ `mvn javafx:run` lanza la app sin errores
- ✅ Pantalla de inicio muestra los dos botones
- ✅ Banner DEMO visible con contador
- ✅ Wizard de primera configuración guarda en SQLite
- ✅ Pantalla de activación muestra ID del equipo
- ✅ `GeneradorClaves.java` genera claves validables

---

## Tareas

### 1.1 — pom.xml
```
Lee [[CLAUDE.md]] y [[01-arquitectura/01-04-configuracion-maven]].
Genera /app/pom.xml completo.
Verifica con: mvn compile
```

### 1.2 — Modelos de datos
```
Lee [[CLAUDE.md]] y [[03-app-core/03-03-modelos-datos]].
Genera todas las clases modelo y enums.
```

### 1.3 — Base de datos y repositories
```
Lee [[CLAUDE.md]], [[10-base-de-datos/10-01-esquema-completo]] y [[10-base-de-datos/10-02-migraciones]].
Genera DatabaseManager.java + todos los Repositories.
```

### 1.4 — ConfiguracionManager
```
Lee [[CLAUDE.md]] y [[03-app-core/03-05-configuracion-negocio]].
Genera ConfiguracionManager.java como singleton.
```

### 1.5 — Sistema de licencias
```
Lee [[CLAUDE.md]], [[06-licencias/06-04-hardware-fingerprint]],
[[06-licencias/06-02-activacion-clave]] y [[06-licencias/06-01-modo-demo]].
Genera en orden: HardwareFingerprint → LicenciaManager → ModoDemo →
GeneradorClaves → ActivacionController + activacion.fxml
```

### 1.6 — Main.java y pantalla de inicio
```
Lee [[CLAUDE.md]] y [[03-app-core/03-01-main-java]].
Genera Main.java + inicio.fxml + InicioController.java +
wizard-primer-arranque.fxml + PrimerArranqueController.java
```

---

## Verificación

```bash
cd app && mvn javafx:run
# 1. Aparece wizard en primera ejecución
# 2. Después: pantalla de inicio con 2 botones + banner DEMO
# 3. Clic "Activar licencia" → muestra ID del equipo
# 4. java GeneradorClaves [ID] → genera clave
# 5. Introducir clave → banner DEMO desaparece
```
