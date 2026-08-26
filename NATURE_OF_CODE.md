# Roadmap: soporte de runtime para The Nature of Code

Objetivo: ampliar `runtime/` lo suficiente para poder programar los ejercicios del libro
*The Nature of Code* (Daniel Shiffman) con este Mode. Pensado para que este documento sirva
de referencia mientras se implementa a mano, hito a hito.

## Dónde va cada cosa

| Qué | Dónde |
|---|---|
| Headers públicos del runtime | `runtime/include/processing/*.h` — cada uno declara una parte de la API; `Processing.h` los incluye todos |
| Implementación del runtime | `runtime/src/*.cpp` — `Window.cpp` (ventana/loop/input), `Renderer.cpp` (dibujo) |
| Ejemplos del Mode | `mode/examples/<Categoría>/<Subcategoría>/<Nombre>/<Nombre>.cpp` — cada carpeta es un sketch que aparece en el menú *Examples* del Mode (ver `mode/examples/Basics/` para la convención; usa `.hpp`+`.cpp` auxiliares si el ejemplo necesita una clase, como `Basics/Objects/Bouncer`) |
| Plantilla que envuelve el sketch | `runtime/src/main_template.cpp.in` (no debería hacer falta tocarla) |

## Ciclo de prueba rápido, sin abrir el PDE cada vez

```bash
# Si tocaste mode/src/*.java, recompílalo primero:
javac -d build/java-classes mode/src/CppSketch.java mode/src/CppBuild.java

# Compila y enlaza un ejemplo/sketch directamente contra el runtime:
java -cp build/java-classes processing.mode.cpp.CppBuild \
  mode/examples/<tu-carpeta> runtime/src/main_template.cpp.in \
  runtime/include runtime/src build/prueba
```

Cuando quieras verlo instalado de verdad dentro del PDE:

```bash
./scripts/package-mode.sh /tmp/processing4-dist/Processing   # o la ruta de tu instalación
```

y copiar `dist/CppModeDev/` a `~/sketchbook/modes/CppModeDev/`, reiniciar Processing.

## Hitos

### Hito 1 — Math básico
No toca el renderer; puede ir todo `inline`/`constexpr` en un header nuevo.

- `runtime/include/processing/Math.h` (nuevo):
  - Constantes: `PI`, `TWO_PI`, `HALF_PI`, `QUARTER_PI`.
  - Funciones: `map()`, `constrain()`, `dist()`, `lerp()`.
- Añadir el include a `Processing.h`.

### Hito 2 — `random()` / `noise()`

- `runtime/include/processing/Random.h` + `runtime/src/Random.cpp`:
  - `random(max)`, `random(min, max)`, `randomSeed(seed)` — trivial con `<random>`.
  - `noise()` (ruido Perlin) — más elaborado. Recomendado: portar una implementación de
    referencia del "Improved Perlin Noise" de Ken Perlin (dominio público, bien documentada)
    en vez de escribirlo desde cero.

### Hito 3 — `PVector`
El más rentable: desbloquea los capítulos 1 (Vectores), 2 (Fuerzas), 3 (Oscilación), 4
(Sistemas de partículas) y buena parte del 6 (Agentes autónomos).

- `runtime/include/processing/PVector.h` (nuevo):
  - Campos `x`, `y` (float).
  - `add()`, `sub()`, `mult()`, `div()` — estilo Processing: mutan el propio objeto.
  - `mag()`, `magSq()`, `normalize()`, `limit()`, `setMag()`, `heading()`.
  - `dist()` estático entre dos vectores.
- Clase pequeña, puede quedar entera `inline` en el header, sin `.cpp`.

### Hito 4 — `translate()` / `rotate()` / `pushMatrix()` / `popMatrix()`
El único hito que cambia de verdad cómo dibuja el runtime — por eso al final.

- Necesita una pila de transformaciones (traslación + rotación + escala) aplicada a cada
  shape antes de dibujar.
- Problema concreto: `rect()`/`ellipse()` actuales usan `SDL_RenderFillRect`
  (`runtime/src/Renderer.cpp`), que solo dibuja rectángulos alineados a los ejes. Con
  rotación ya no vale — hay que calcular los 4 vértices a mano y dibujarlos con
  `SDL_RenderGeometry`, como ya hace `triangle()`.
- Necesario para el capítulo 8 (Fractales, ramas rotadas) y para orientar formas según su
  dirección de movimiento en el capítulo 6.

## Estado

Sin empezar. Ver [`CLAUDE.md`](./CLAUDE.md) para el estado general del proyecto.
