# processing-cpp-mode

## Nota sobre processing-cpp/processing.cpp
Existe **[processing-cpp/processing.cpp](https://github.com/processing-cpp/processing.cpp)**,
un Mode C++ para Processing4 mucho más maduro (parser de C++ propio con AST, GLFW+GLEW,
caché de compilación incremental, instalable vía Contribution Manager, ~400 commits desde
mayo de 2026). Se evaluó pararse aquí y usar ese proyecto en su lugar, pero se decidió
continuar este como proyecto propio — con objetivos concretos que ese proyecto no cubre tal
cual (ver más abajo) — usándolo como referencia de arquitectura, no como sustituto directo.

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
│   │   ├── CppModeDev.java
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
3. **Integración PDE (Java)** — CppModeDev, invocación del compilador, parseo de errores a líneas
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
Retomado en Fase 4. **El Mode ya es instalable y se validó de extremo a extremo dentro de una
instancia real del PDE, en dos versiones distintas**: Processing 4.0.1 (UI Swing clásica) y
**4.5.6, la más reciente** (UI Compose Desktop) — aparece "C++ (dev)" en el desplegable de
modos, se puede abrir un ejemplo o crear un sketch nuevo, pulsar Play, y el binario compila y
se ejecuta de verdad (confirmado viendo la ventana SDL2 del sketch abierta). Se llama
"C++ (dev)" y no "C++ Mode"/"CppMode" a propósito, para no chocar con
processing-cpp/processing.cpp si conviven en el mismo sketchbook (Processing exige que
carpeta, .jar y clase Java compartan nombre — ver `mode/src/CppModeDev.java`).

Resumen de lo validado:
- **Fase 0-1 (runtime C++/SDL2)**: `Sketch.h`, bucle con `frameRate()` real, primitivas
  (`rect`/`ellipse`/`triangle`/`point`/`line`), input (`mouseX`/`pmouseX`/`mouseButton`...),
  compilado y ejecutado a mano con éxito.
- **Fase 2 (preprocesador)**: `CppSketch.java` concatena tabs y las inyecta en
  `main_template.cpp.in`; validado por CLI generando y compilando el `.cpp` resultante.
- **Fase 3 (integración PDE)**: `CppModeDev`/`CppEditor`/`CppBuild`/`CppRunner` compilan
  limpio contra la API real de `processing.app.*` (probado contra Processing 4.5.6 y 4.0.1).
  `CppBuild` invoca g++ y mapea errores a la tab/línea original del sketch.
- **Fase 4 (UX, en curso)**: salto a línea de error vía `SketchException` implementado;
  **empaquetado e instalación real validados** (`scripts/package-mode.sh` + prueba manual en
  un sketchbook real: seleccionar el modo, crear sketch, Play, ejecutar — funcionó). Quedan
  ejemplos adicionales y pulido de Play/Stop.
- **Notas del proceso de empaquetado** (para no repetir los mismos fallos): el `.jar` hay
  que compilarlo con `javac --release 17` (el runtime embebido en Processing 4.0.1 no carga
  bytecode más nuevo, y usamos `record` que exige mínimo Java 16); `mode.properties` no
  admite continuación de línea con `\` (el parser de Settings.java la marca como "illegal
  line"); `keywords.txt` necesita al menos una línea válida `<keyword>\t<coloring>`, si no el
  token marker queda sin inicializar y el editor peta con NPE al abrir cualquier sketch.
- **Nota sobre lanzar Processing 4.5.6 en un sandbox sin GUI completa**: el lanzador nativo
  de jpackage (`bin/Processing`) puede fallar con `InaccessibleObjectException` en
  `LinuxPlatform.initBase` (falta `--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED`), y
  editar `lib/app/Processing.cfg` para añadirlo puede hacer que el propio lanzador falle en
  silencio (sin salida, sin excepción) por razones no del todo claras. La solución que
  funcionó: invocar el JDK completo que trae empaquetado en
  `lib/app/resources/jdk/bin/java` directamente, con el classpath y las `-D` que aparecen en
  `Processing.cfg` (sustituyendo `$APPDIR` por `lib/app`), más
  `--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED`, y `processing.app.ProcessingKt` como
  clase principal. No es un problema de este proyecto — es del empaquetado de Processing4
  para Linux en este entorno concreto — pero ahorra tiempo la próxima vez.
