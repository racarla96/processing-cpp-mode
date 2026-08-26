# processing-cpp-mode

Un "Mode" para [Processing4](https://github.com/processing/processing4) que permite escribir
sketches en C++ dentro del PDE, con la misma experiencia de uso que Python Mode: escribes
código, pulsas Play, se compila (breve fricción aceptable) y se ejecuta.

No se busca hot-reload real (no Cling, no Clang-REPL, no dylib swapping) — el modelo es
compilar-y-ejecutar, igual que ya hace el modo Java internamente: guardar → Play → compilar →
lanzar proceso nuevo.

Ver [`CLAUDE.md`](./CLAUDE.md) para el detalle de arquitectura, estructura del repo y fases de
desarrollo.

## Estado

En Fase 4 de 5. **Validado de extremo a extremo dentro de una instancia real del PDE, en
Processing 4.0.1 y en la 4.5.6 (la más reciente)**: instalar, seleccionar "C++ (dev)" en el
desplegable de modos, abrir un ejemplo o crear un sketch nuevo, pulsar Play, y el binario
compila y se ejecuta. Ver el detalle en [`CLAUDE.md`](./CLAUDE.md).

## Instalar

```
scripts/package-mode.sh <ruta-a-una-instalación-de-Processing4>
```

Genera `dist/CppModeDev/`, listo para copiar a `<sketchbook>/modes/CppModeDev/`. Se llama
"Dev" para no chocar con [processing-cpp/processing.cpp](https://github.com/processing-cpp/processing.cpp)
si tienes ese Mode instalado (Processing exige que carpeta, .jar y clase Java compartan
nombre). Reinicia Processing y selecciona **C++ (dev)** en el desplegable de modos (arriba a
la derecha).

## Licencia

GPL-2.0. Ver [`LICENSE`](./LICENSE).
