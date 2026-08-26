// main.cpp — Ejemplo de validación técnica (Fase 0).
//
// Combina a mano lo que en fases posteriores generará CppSketch.java a
// partir de un sketch.cpp de usuario + runtime/src/main_template.cpp.in:
// setup()/draw() (lo único que escribirá el usuario) más un main() que
// llama a processing::run(). Sirve para validar que el runtime compila
// y corre antes de tocar Java/PDE.
//
// Controles: mover el ratón mueve el círculo; Escape o cerrar la
// ventana termina el sketch.

#include "processing/Processing.h"

void setup() {
    processing::size(640, 360);
}

void draw() {
    processing::background(30, 30, 40);

    processing::fill(60, 140, 220);
    processing::stroke(255, 255, 255);
    processing::rect(20, 20, 100, 60);

    processing::fill(220, 60, 60);
    processing::ellipse((float)processing::mouseX, (float)processing::mouseY, 80, 80);
}

int main(int argc, char** argv) {
    return processing::run(argc, argv);
}
