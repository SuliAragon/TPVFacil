# 01-04 — Configuración Maven (pom.xml)

#arquitectura #fase-1
Relacionado: [[CLAUDE.md]] | [[01-02-stack-tecnologico]] | [[11-fases/11-02-fase1-nucleo]]
Estado: #pendiente

---

## Dependencias Requeridas

```xml
<!-- JavaFX 21 -->
org.openjfx:javafx-controls:21
org.openjfx:javafx-fxml:21

<!-- SQLite → [[10-base-de-datos/10-01-esquema-completo]] -->
org.xerial:sqlite-jdbc:3.45.3.0

<!-- Criptografía → [[02-verifactu/02-06-certificados]] -->
org.bouncycastle:bcprov-jdk18on:1.78
org.bouncycastle:bcpkix-jdk18on:1.78

<!-- XML Verifactu → [[02-verifactu/02-04-xml-schema]] -->
jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
com.sun.xml.bind:jaxb-impl:4.0.5

<!-- QR → [[02-verifactu/02-07-qr-ticket]] -->
com.google.zxing:core:3.5.3
com.google.zxing:javase:3.5.3

<!-- Logging -->
org.slf4j:slf4j-api:2.0.13
ch.qos.logback:logback-classic:1.5.6
```

## Plugins Maven Requeridos

```xml
<!-- Compilar con Java 21 -->
maven-compiler-plugin → source/target: 21, encoding: UTF-8

<!-- Ejecutar en desarrollo -->
javafx-maven-plugin → mvn javafx:run

<!-- Fat JAR con dependencias → [[07-instalador/07-01-jpackage]] -->
maven-shade-plugin → genera tpvfacil-VERSION-shaded.jar
                     Main-Class: com.tpvfacil.Main
```

## Comandos de Uso

```bash
mvn javafx:run          # Desarrollo
mvn clean package       # Genera el fat JAR
./build.sh              # Genera el instalador .exe → [[07-instalador/07-03-proceso-build]]
```

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera /app/pom.xml completo con:
- groupId: com.tpvfacil, artifactId: tpvfacil, version: 1.0.0
- Todas las dependencias listadas arriba con sus versiones exactas
- Plugin maven-compiler-plugin: Java 21, UTF-8
- Plugin javafx-maven-plugin configurado para com.tpvfacil.Main
- Plugin maven-shade-plugin: fat JAR con nombre tpvfacil-1.0.0-shaded.jar,
  Main-Class en MANIFEST.MF, transformers para fusionar META-INF/services
- Módulos JavaFX necesarios: controls, fxml, graphics, base, swing (para impresión)
```
