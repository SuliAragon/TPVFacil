# 11-06 — Fase 5: Instalador

#fase #fase-5
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[07-instalador/07-01-jpackage]] | [[07-instalador/07-03-proceso-build]]
Prerequisito: Fase 1 ✅ + Fase 2 ✅ + (Fase 3 o Fase 4) ✅
Estado: #pendiente

---

## Objetivo

Generar instaladores nativos para los tres sistemas operativos principales mediante GitHub Actions (CI/CD automático al publicar un tag `v*`).

| Sistema | Formato | Herramienta |
|---------|---------|-------------|
| **Windows** | `.exe` (NSIS/WiX) | `jpackage --type exe` |
| **macOS** | `.dmg` | `jpackage --type dmg` |
| **Linux** | `.deb` + `.rpm` + `.AppImage` | `jpackage --type deb/rpm` |

Todos incluyen el JRE 21 embebido — el usuario final **no necesita instalar Java**.

---

## Criterio de Éxito

- ✅ `TPVFacil-1.0.0-windows.exe` funciona en Windows 10/11 limpio
- ✅ `TPVFacil-1.0.0-mac.dmg` funciona en macOS 13+ limpio
- ✅ `TPVFacil-1.0.0-linux.deb` funciona en Ubuntu 22.04+
- ✅ `TPVFacil-1.0.0-linux.rpm` funciona en Fedora/RHEL
- ✅ En ningún caso el usuario necesita instalar Java
- ✅ Datos guardados en directorio correcto por SO (AppData / Library / ~/.local)
- ✅ Desinstalación limpia en cada plataforma
- ✅ GitHub Actions genera los 4 artefactos automáticamente con un solo tag

---

## Prerequisitos Externos

- **Windows:** WiX Toolset 3.x (runner `windows-latest` en GitHub Actions)
- **macOS:** Xcode Command Line Tools (runner `macos-latest`)
- **Linux:** `fakeroot` + `rpm` (runner `ubuntu-latest`)
- Icono `.ico` (Windows), `.icns` (macOS), `.png` (Linux)

---

## Rutas de datos por sistema operativo

| SO | Ruta base |
|----|-----------|
| Windows | `%LOCALAPPDATA%\TPVFacil\` |
| macOS | `~/Library/Application Support/TPVFacil/` |
| Linux | `~/.local/share/TPVFacil/` |

El `DatabaseManager.java` detecta el SO y usa la ruta correcta.

---

## Tareas

### 5.1 — Verificar fat JAR y Main-Class
```
Confirmar que pom.xml tiene maven-shade-plugin configurado correctamente
con Main-Class: com.tpvfacil.Main en MANIFEST.MF.
```

### 5.2 — Scripts de build locales
```
Lee [[CLAUDE.md]], [[07-instalador/07-01-jpackage]] y [[07-instalador/07-03-proceso-build]].
Genera:
  App/build.sh  → build para macOS y Linux
  App/build.bat → build para Windows
Detectan versión desde pom.xml automáticamente.
```

### 5.3 — GitHub Actions (workflow multi-plataforma)
```
Lee [[CLAUDE.md]] y [[07-instalador/07-01-jpackage]].
Genera .github/workflows/release.yml:
- Trigger: push de tags v* (ej. v1.0.0)
- 3 jobs paralelos: build-windows, build-macos, build-linux
  · build-windows (windows-latest): genera .exe
  · build-macos   (macos-latest):   genera .dmg
  · build-linux   (ubuntu-latest):  genera .deb y .rpm
- Job final: crea GitHub Release y sube los 4 artefactos
```

### 5.4 — Verificar rutas AppData / Library / .local
```
Confirmar que DatabaseManager.java detecta el SO correctamente
y usa la ruta de datos apropiada en cada plataforma.
```

### 5.5 — Prueba en entorno limpio (cada plataforma)
```
Windows 10/11: instalar .exe → wizard → venta → datos persisten → desinstalar
macOS 13+:     instalar .dmg → wizard → venta → datos persisten → desinstalar
Ubuntu 22.04:  instalar .deb → wizard → venta → datos persisten → desinstalar
```
