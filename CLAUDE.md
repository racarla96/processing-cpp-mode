# processing-cpp-mode

## ⚠️ Proyecto detenido (26/08/2026)
Durante la Fase 4 descubrimos que ya existe **[processing-cpp/processing.cpp](https://github.com/processing-cpp/processing.cpp)**,
un Mode C++ para Processing4 mucho más maduro que resuelve el mismo problema descrito
abajo: mismo modelo compilar-y-ejecutar, sin hot-reload, `setup()`/`draw()` dentro del
PDE. Va más lejos que este repo en varios frentes: doble modo de uso (Mode instalable vía
Contribution Manager *y* librería header-only standalone fuera del PDE), soporte real de
Windows/macOS/Linux con CI y releases descargables, y sitio de documentación propio. Activo
(commits hasta días antes de esta nota), LGPL-2.1, usa GLFW+GLEW en vez de SDL2.

Decisión: dejar de duplicar esfuerzo y usar/contribuir a ese proyecto en vez de continuar
este. Este repo se conserva como está (funcional hasta donde se validó: Fases 0-3 probadas
de extremo a extremo, Fase 4 parcialmente implementada — ver historial de commits) a modo de
referencia/aprendizaje, sin desarrollo activo previsto.

## Objetivo
Crear un "Mode" nuevo para Processing4 (https://github.com/processing/processing4) que permita
escribir sketches en C++ dentro del PDE, con la misma experiencia de uso que Python Mode:
escribes código, pulsas Play, se compila (breve fricción aceptable) y se ejecuta. NO se busca
hot-reload real (no Cling, no Clang-REPL, no dylib swapping) — el modelo es compilar-y-ejecutar,
igual que ya hace el modo Java internamente: guardar → Play → compilar → lanzar proceso nuevo.

## Decisiones de arquitectura ya tomadas
- Se evaluaron 3 enfoques: (A) intérprete C++ tipo Cling/Clang-REPL, (B) recarga de librería
  dinámica (dlopen, patrón "Handmade Hero"), (C) compilar y ejecutar como proceso nuevo.
  -> Se eligió (C) por simplicidad. Es el patrón target de este proyecto.
- El "Mode" (integración con el PDE) es Java, igual que los demás modos de Processing.
- El "Runtime" (librería que reemplaza core.jar) es C++ puro, sobre SDL2/OpenGL.
- Preprocesador: el sketch del usuario solo tiene `setup()` y `draw()` (sin `main()`), igual que
  un sketch Processing normal; el Mode genera el archivo real a compilar inyectando `main()` y el
  bucle de eventos.

## Estructura del repo
```
processing-cpp-mode/
├── mode/                      # Integración PDE (Java)
│   ├── src/
│   │   ├── CppMode.java
│   │   ├── CppEditor.java
│   │   ├── CppBuild.java       # invoca g++/clang++, parsea errores
│   │   ├── CppSketch.java      # preprocesador (envuelve el sketch del usuario)
│   │   └── CppRunner.java      # lanza/mata el proceso compilado
│   ├── mode.properties
│   ├── keywords.txt
│   └── theme/
├── runtime/                   # Librería C++ (equivalente a core.jar)
│   ├── include/processing/
│   │   ├── Sketch.h            # clase base con setup()/draw()
│   │   ├── Shapes.h            # rect(), ellipse(), line()...
│   │   ├── Color.h             # fill(), stroke(), background()
│   │   ├── Input.h              # mouseX, mouseY, keyPressed...
│   │   └── Processing.h         # header único (incluye todo)
│   ├── src/
│   │   ├── Window.cpp            # SDL2/GLFW + contexto OpenGL
│   │   ├── Renderer.cpp          # implementación de primitivas
│   │   └── main_template.cpp.in  # plantilla que envuelve el sketch
│   └── CMakeLists.txt
├── templates/
│   └── default/                # sketch de ejemplo al crear uno nuevo
└── examples/
```

## Fases de desarrollo
0. **Validación técnica** (actual) — Sketch.h mínimo, compilar y correr un ejemplo a mano
   (sin tocar Java/PDE todavía). Si esto no es fluido, replantear.
1. **Runtime C++** — ventana/loop, primitivas 2D, variables globales (width, height, mouseX...),
   input básico, empaquetado como librería estática.
2. **Preprocesador/plantillas** — transformar el .cpp del usuario en el archivo compilable real.
3. **Integración PDE (Java)** — CppMode, invocación del compilador, parseo de errores a líneas
   del editor, CppRunner para gestionar el proceso hijo.
4. **UX** — Play/Stop, consola de errores clickeable, ejemplos, ocultar la plantilla al usuario.
5. **Pulido** — soporte 3D, autocompletado (clangd), compilación incremental.

## Riesgos a vigilar desde el principio
- Legibilidad de errores del compilador (crítico para que se sienta como Python Mode).
- Portabilidad del toolchain, especialmente Windows (MinGW vs MSVC).
- Velocidad de compilación del sketch del usuario.

## Licencia
GPL-2.0 para todo el repo. El módulo `mode/` se integra directamente con clases del PDE
(Editor, Mode, etc. de processing4), que es GPL-2.0, así que un derivado debe serlo también.
Se usa GPL-2.0 también en `runtime/` por simplicidad y coherencia con el resto del proyecto,
aunque técnicamente es código independiente. Referencia: la librería core de Processing es
LGPL-2.1, pero "todo lo demás incluyendo el PDE" es GPL-2.0 (ver LICENSE.md de processing4).

## Estado actual
Detenido en Fase 4 (ver nota al principio del documento). Resumen de lo validado antes de
parar:
- **Fase 0-1 (runtime C++/SDL2)**: `Sketch.h`, bucle con `frameRate()` real, primitivas
  (`rect`/`ellipse`/`triangle`/`point`/`line`), input (`mouseX`/`pmouseX`/`mouseButton`...),
  compilado y ejecutado a mano con éxito.
- **Fase 2 (preprocesador)**: `CppSketch.java` concatena tabs y las inyecta en
  `main_template.cpp.in`; validado por CLI generando y compilando el `.cpp` resultante.
- **Fase 3 (integración PDE)**: `CppMode`/`CppEditor`/`CppBuild`/`CppRunner` compilan limpio
  contra la API real de `processing.app.*` (probado contra Processing 4.5.6 y 4.0.1).
  `CppBuild` invoca g++ y mapea errores a la tab/línea original del sketch.
- **Fase 4 (UX)**: salto a línea de error vía `SketchException` implementado; ejemplos y
  pulido de Play/Stop quedaron sin terminar.
- **Sin validar**: correr el Mode dentro de una instancia real del PDE gráfico (se intentó
  con Processing 4.5.6, que usa una UI reescrita en Compose Desktop incompatible con este
  sandbox de desarrollo; con 4.0.1, clásica Swing, sí llegó a abrir ventana, pero no se
  completó la instalación del Mode antes de encontrar processing-cpp/processing.cpp).
