// Sketch.h
//
// Núcleo mínimo del runtime (Fase 0 - Validación técnica): variables
// globales width/height, size() y el bucle principal run(), que crea la
// ventana y llama a setup()/draw() del usuario.
//
// El sketch del usuario NO define una clase: solo implementa las
// funciones libres setup() y draw() (declaradas al final de este
// archivo), igual que un sketch Processing normal. En fases posteriores
// CppSketch.java inyectará esas funciones en la plantilla
// runtime/src/main_template.cpp.in junto con un main() que llama a
// processing::run(); en Fase 0 ese main() se escribe a mano (ver
// examples/hello_circle/main.cpp) para validar el flujo sin tocar
// Java/PDE.

#pragma once

namespace processing {

extern int width;
extern int height;
extern int frameCount;

// Fija el tamaño de la ventana. Solo tiene efecto si se llama desde
// setup(), antes de que run() cree la ventana (limitación conocida de
// Fase 0; redimensionar en caliente se evaluará más adelante).
void size(int w, int h);

// Fija los fotogramas por segundo objetivo del bucle (60 por defecto).
// Se puede llamar en cualquier momento, normalmente desde setup().
void frameRate(float fps);

// Inicializa SDL2, crea la ventana, llama a setup() una vez y luego a
// draw() en cada frame hasta que el usuario cierra la ventana o pulsa
// Escape. Devuelve el código de salida del proceso.
int run(int argc, char** argv);

} // namespace processing

// Funciones que debe implementar el sketch del usuario.
void setup();
void draw();
