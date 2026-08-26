// sketch.cpp
//
// Sketch de ejemplo por defecto al crear un nuevo sketch C++ en el PDE.
// Solo define setup() y draw(), igual que un sketch Processing normal;
// el Mode inyecta main() a partir de esto (ver
// runtime/src/main_template.cpp.in), así que no hace falta #include ni
// prefijo processing:: en las llamadas.

void setup() {
    size(640, 360);
}

void draw() {
    background(200, 200, 200);
}
