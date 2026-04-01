# 07-03 — Proceso de Build

#instalador #fase-5
Relacionado: [[CLAUDE.md]] | [[07-01-jpackage]] | [[07-02-firma-ejecutable]]
Estado: #pendiente

---

## Checklist de Release

```
PRE-BUILD
[ ] Actualizar version en pom.xml
[ ] Actualizar APP_VERSION en ConfiguracionManager
[ ] Correr tests: mvn test

BUILD
[ ] mvn clean package -DskipTests
[ ] jpackage → ver [[07-01-jpackage]]
[ ] Verificar que el .exe existe en instalador/

PRUEBAS
[ ] Windows 10 limpio (sin Java) → instalar → funciona
[ ] Windows 11 limpio → instalar → funciona
[ ] Flujo completo: configurar → demo → ticket → Verifactu sandbox

DISTRIBUCIÓN
[ ] Subir .exe a GitHub Releases con tag vX.Y.Z
[ ] Actualizar URL en [[08-web/08-06-pagina-descarga]]
[ ] git tag vX.Y.Z && git push origin vX.Y.Z
[ ] Actualizar sitemap.xml con nueva fecha → [[09-seo/09-04-seo-tecnico]]
```

## Datos Persistentes (AppData)

```java
// Windows: C:\Users\[usuario]\AppData\Local\TPVFacil\
// Al detectar OS en DatabaseManager:
String os = System.getProperty("os.name").toLowerCase();
if (os.contains("win")) {
    return Path.of(System.getenv("LOCALAPPDATA"), "TPVFacil", "tpvfacil.db");
}
```

Esto garantiza que los datos **sobreviven** a actualizaciones y desinstalaciones.
