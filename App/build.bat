@echo off
REM ─────────────────────────────────────────────────────────────
REM TPVFácil — Script de build local (Windows)
REM
REM Uso:
REM   build.bat            -> genera el instalador .exe
REM   build.bat --jar-only -> solo genera el fat JAR
REM
REM Requisitos:
REM   · Java 21+ en el PATH (o JAVA_HOME configurado)
REM   · Maven 3.8+ en el PATH
REM   · WiX Toolset 3.x  (choco install wixtoolset)
REM ─────────────────────────────────────────────────────────────

setlocal enabledelayedexpansion

REM ── Detectar versión desde pom.xml ────────────────────────────
for /f "tokens=2 delims=><" %%a in ('findstr /n "<version>" pom.xml') do (
    if not defined VERSION set VERSION=%%a
)
echo TPVFacil v%VERSION% — Build local
echo ──────────────────────────────────

REM ── 1. Compilar y generar fat JAR ─────────────────────────────
echo ^-^> Compilando con Maven...
call mvn clean package -q
if errorlevel 1 (
    echo [ERROR] Fallo en mvn package
    exit /b 1
)
echo ^[OK^] JAR generado: target\tpvfacil-%VERSION%-shaded.jar

if "%1"=="--jar-only" (
    echo ^[OK^] Listo (solo JAR).
    exit /b 0
)

REM ── 2. Generar instalador Windows (.exe) ──────────────────────
echo ^-^> Generando instalador Windows (.exe)...
if not exist dist mkdir dist

jpackage ^
    --name "TPVFacil" ^
    --app-version "%VERSION%" ^
    --description "TPV para negocios espanoles con Verifactu (AEAT)" ^
    --vendor "TPVFacil" ^
    --input target ^
    --main-jar "tpvfacil-%VERSION%-shaded.jar" ^
    --main-class com.tpvfacil.Main ^
    --type exe ^
    --win-menu ^
    --win-menu-group "TPVFacil" ^
    --win-shortcut ^
    --win-dir-chooser ^
    --win-upgrade-uuid "8B3E2F1A-4C5D-4E6F-9A0B-1C2D3E4F5A6B" ^
    --java-options "--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED" ^
    --dest dist

if errorlevel 1 (
    echo [ERROR] Fallo en jpackage. Comprueba que WiX Toolset este instalado.
    echo         choco install wixtoolset
    exit /b 1
)

echo.
echo [OK] Instalador creado en: dist\
dir dist\*.exe
echo.
echo Instalacion completada.
