#!/bin/bash
# ─────────────────────────────────────────────────────────────
# TPVFácil — Script de build local (macOS / Linux)
#
# Uso:
#   chmod +x build.sh
#   ./build.sh            → genera instalador para tu plataforma
#   ./build.sh --jar-only → solo genera el fat JAR sin instalar
#
# Requisitos:
#   · Java 21+ (JAVA_HOME apuntando al JDK correcto)
#   · Maven 3.8+
# ─────────────────────────────────────────────────────────────

set -e

# ── Detectar versión desde pom.xml ────────────────────────────
VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
echo "TPVFácil v$VERSION — Build local"
echo "─────────────────────────────────"

# ── 1. Compilar y generar fat JAR ─────────────────────────────
echo "→ Compilando con Maven..."
mvn clean package -q
echo "✓ JAR generado: target/tpvfacil-${VERSION}-shaded.jar"

if [[ "$1" == "--jar-only" ]]; then
    echo "✓ Listo (solo JAR)."
    exit 0
fi

# ── 2. Detectar SO y generar instalador ───────────────────────
OS=$(uname -s)
mkdir -p dist

if [[ "$OS" == "Darwin" ]]; then
    echo "→ Generando instalador macOS (.dmg)..."
    jpackage \
        --name "TPVFacil" \
        --app-version "$VERSION" \
        --description "TPV para negocios españoles con Verifactu (AEAT)" \
        --vendor "TPVFácil" \
        --input target \
        --main-jar "tpvfacil-${VERSION}-shaded.jar" \
        --main-class com.tpvfacil.Main \
        --type dmg \
        --mac-package-name "TPVFácil" \
        --java-options "--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED" \
        --dest dist
    echo "✓ Instalador creado en: dist/"
    ls dist/*.dmg

elif [[ "$OS" == "Linux" ]]; then
    echo "→ Generando instalador Linux (.deb)..."
    jpackage \
        --name "tpvfacil" \
        --app-version "$VERSION" \
        --description "TPV para negocios espanoles con Verifactu (AEAT)" \
        --vendor "TPVFacil" \
        --input target \
        --main-jar "tpvfacil-${VERSION}-shaded.jar" \
        --main-class com.tpvfacil.Main \
        --type deb \
        --linux-menu-group "Office" \
        --linux-shortcut \
        --java-options "--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED" \
        --dest dist
    echo "✓ Instalador creado en: dist/"
    ls dist/*.deb

else
    echo "❌ SO no reconocido: $OS. Usa build.bat en Windows."
    exit 1
fi

echo ""
echo "✅ Build completado."
