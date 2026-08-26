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

Fase 0 (Validación técnica) en curso: escribir un `Sketch.h` mínimo y validar que un ejemplo
compila y corre antes de tocar Java/PDE.

## Licencia

GPL-2.0. Ver [`LICENSE`](./LICENSE).
