# processing-cpp-mode

> ⚠️ **Proyecto detenido.** Ya existe [processing-cpp/processing.cpp](https://github.com/processing-cpp/processing.cpp),
> un Mode C++ para Processing4 más maduro que resuelve el mismo problema (compilar-y-ejecutar,
> sin hot-reload, `setup()`/`draw()` dentro del PDE), con soporte multiplataforma real,
> instalación vía Contribution Manager, y uso también como librería standalone. Este repo se
> conserva como referencia; ver la nota en [`CLAUDE.md`](./CLAUDE.md) para el detalle.

Un "Mode" para [Processing4](https://github.com/processing/processing4) que permite escribir
sketches en C++ dentro del PDE, con la misma experiencia de uso que Python Mode: escribes
código, pulsas Play, se compila (breve fricción aceptable) y se ejecuta.

No se busca hot-reload real (no Cling, no Clang-REPL, no dylib swapping) — el modelo es
compilar-y-ejecutar, igual que ya hace el modo Java internamente: guardar → Play → compilar →
lanzar proceso nuevo.

Ver [`CLAUDE.md`](./CLAUDE.md) para el detalle de arquitectura, estructura del repo y fases de
desarrollo.

## Estado

Detenido en Fase 4 de 5. Fases 0-3 (runtime C++, preprocesador, integración con la API real
del PDE) validadas de extremo a extremo; ver el detalle en [`CLAUDE.md`](./CLAUDE.md).

## Licencia

GPL-2.0. Ver [`LICENSE`](./LICENSE).
