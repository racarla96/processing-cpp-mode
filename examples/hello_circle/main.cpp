// main.cpp — Ejemplo de validación técnica.
//
// Combina a mano lo que en fases posteriores generará CppSketch.java a
// partir de un sketch.cpp de usuario + runtime/src/main_template.cpp.in:
// setup()/draw() (lo único que escribirá el usuario) más un main() que
// llama a processing::run(). Sirve para validar que el runtime compila
// y corre antes de tocar Java/PDE.
//
// Ejercita también las adiciones de Fase 1: frameRate(), noFill()/
// noStroke(), pmouseX/pmouseY y triangle().
//
// Controles: mover el ratón mueve el círculo (que se vacía mientras se
// mantiene pulsado); Escape o cerrar la ventana termina el sketch.

#include "processing/Processing.h"

void setup() {
    processing::size(640, 360);
    processing::frameRate(60);
}

void draw() {
    processing::background(30, 30, 40);

    processing::noStroke();
    processing::fill(60, 140, 220);
    processing::rect(20, 20, 100, 60);

    processing::stroke(255, 220, 60);
    processing::line((float)processing::pmouseX, (float)processing::pmouseY,
                      (float)processing::mouseX, (float)processing::mouseY);

    processing::stroke(255, 255, 255);
    if (processing::mousePressed) {
        processing::noFill();
    } else {
        processing::fill(220, 60, 60);
    }
    processing::ellipse((float)processing::mouseX, (float)processing::mouseY, 80, 80);

    processing::noStroke();
    processing::fill(80, 200, 120);
    processing::triangle(500, 300, 560, 300, 530, 240);
}

int main(int argc, char** argv) {
    return processing::run(argc, argv);
}
