#!/usr/bin/env bash
#
# package-mode.sh — Genera dist/CppModeDev/, una carpeta lista para
# copiar a <sketchbook>/modes/CppModeDev/ e instalar el Mode C++ a mano
# (sin pasar por el Contribution Manager todavía).
#
# Se llama CppModeDev (no CppMode) porque Processing exige que el .jar,
# la clase Java dentro y la carpeta de instalación compartan nombre, y
# "CppMode" ya lo usa processing-cpp/processing.cpp — un Mode C++ para
# Processing4 distinto (ver CLAUDE.md). Este nombre evita chocar con esa
# instalación si conviven en el mismo sketchbook.
#
# Solo se soporta Processing 4.4+ (layout lib/app/*.jar, UI Compose
# Desktop) — desde que este proyecto se centra en 4.5.6, se dejó de dar
# soporte al layout clásico (lib/pde.jar, Processing <= 4.3.x).
#
# Uso:
#   scripts/package-mode.sh <ruta-a-una-instalación-de-Processing 4.5.6>

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Uso: $0 <ruta-a-una-instalación-de-Processing 4.5.6>" >&2
    exit 1
fi

PROCESSING_HOME="$1"
MODE_NAME="CppModeDev"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$REPO_ROOT/dist/$MODE_NAME"
CLASSES_DIR="$REPO_ROOT/build/mode-classes"

if [ ! -d "$PROCESSING_HOME/lib/app" ]; then
    echo "No se reconoce el layout de Processing en $PROCESSING_HOME" >&2
    echo "(se esperaba lib/app/*.jar, el layout de Processing 4.4+)" >&2
    exit 1
fi
CLASSPATH="$(find "$PROCESSING_HOME/lib/app" -maxdepth 1 -iname '*.jar' | tr '\n' ':')"

echo "== Compilando mode/src contra $PROCESSING_HOME =="
rm -rf "$CLASSES_DIR"
mkdir -p "$CLASSES_DIR"
# --release 17: el runtime embebido en Processing 4.5.6 no carga
# bytecode más nuevo que eso; compilar con el JDK del sistema sin fijar
# esto genera clases que no puede cargar (UnsupportedClassVersionError).
# 17 es también el mínimo real porque usamos records (Java 16+).
javac --release 17 -d "$CLASSES_DIR" -cp "$CLASSPATH" "$REPO_ROOT"/mode/src/*.java

echo "== Empaquetando $DIST_DIR =="
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/mode"

jar cf "$DIST_DIR/mode/$MODE_NAME.jar" -C "$CLASSES_DIR" .

cp "$REPO_ROOT/mode/mode.properties" "$DIST_DIR/mode.properties"
cp "$REPO_ROOT/mode/keywords.txt" "$DIST_DIR/keywords.txt"
cp -r "$REPO_ROOT/mode/theme" "$DIST_DIR/theme"
cp -r "$REPO_ROOT/mode/examples" "$DIST_DIR/examples"

# Mode.getTemplateFolder() espera <carpeta del Mode>/template/sketch.<ext>
# directamente (sin subcarpeta); templates/default/ es donde vive en
# este repo, así que se copia el contenido, no la carpeta en sí.
mkdir -p "$DIST_DIR/template"
cp -r "$REPO_ROOT/templates/default/." "$DIST_DIR/template/"

# El runtime C++ (headers + fuentes) viaja dentro de la distribución del
# Mode: CppModeDev.getRuntimeRoot() lo busca en <carpeta del Mode>/runtime
# antes que en el layout de desarrollo (mode/ y runtime/ hermanos).
cp -r "$REPO_ROOT/runtime" "$DIST_DIR/runtime"

echo
echo "Listo: $DIST_DIR"
echo "Para instalar: copia esa carpeta a <sketchbook>/modes/$MODE_NAME/ y reinicia Processing."
