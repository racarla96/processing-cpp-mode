# processing-cpp-mode

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
Fase 0 en curso: escribir un Sketch.h mínimo y validar que un ejemplo compila y corre antes
de tocar nada de Java/PDE.
