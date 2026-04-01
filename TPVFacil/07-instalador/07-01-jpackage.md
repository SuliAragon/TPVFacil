# 07-01 — jpackage: Instalador Windows

#instalador #fase-5
Relacionado: [[CLAUDE.md]] | [[07-02-firma-ejecutable]] | [[07-03-proceso-build]] | [[11-fases/11-06-fase5-instalador]]
Estado: #pendiente

---

## Objetivo

Un único `TPVFacil-Setup-1.0.0.exe` que el cliente instala sin necesidad de Java. Incluye la JRE dentro (~120 MB total).

## Comando jpackage

```bash
jpackage \
  --input target/ \
  --name "TPVFacil" \
  --main-jar tpvfacil-1.0.0-shaded.jar \
  --main-class com.tpvfacil.Main \
  --type exe \
  --dest instalador/ \
  --app-version 1.0.0 \
  --vendor "TPVFácil" \
  --win-menu --win-menu-group "TPVFácil" \
  --win-shortcut --win-dir-chooser \
  --icon src/main/resources/images/logo.ico \
  --java-options "-Xmx512m"
```

## Requisitos para el Build

- **Windows** (jpackage solo genera .exe en Windows)
- **WiX Toolset 3.x** instalado (`choco install wixtoolset`)
- O usar **GitHub Actions** → [[07-03-proceso-build]]

## Datos del Usuario → AppData

```java
// NO guardar en Program Files (sin permisos de escritura)
// DatabaseManager usa:
Path.of(System.getenv("LOCALAPPDATA"), "TPVFacil", "tpvfacil.db")
// → C:\Users\[usuario]\AppData\Local\TPVFacil\tpvfacil.db
```

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[01-arquitectura/01-04-configuracion-maven]] y este documento.

1. Actualiza pom.xml con maven-shade-plugin correcto (fat JAR)
2. Genera /app/build.sh (detecta versión desde pom.xml, ejecuta mvn + jpackage)
3. Genera /app/build.bat (equivalente para CMD/PowerShell de Windows)
4. Genera /.github/workflows/build.yml para GitHub Actions (windows-latest,
   instala WiX + Java 21, compila, genera .exe, sube como artifact + GitHub Release)
```
