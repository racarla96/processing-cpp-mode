// Mouse 2D.
//
// Adaptado del ejemplo clásico de Processing. Mover el ratón cambia la
// posición y el tamaño de cada cuadrado. El original usa
// rectMode(CENTER) (no soportado todavía): aquí se calcula la esquina
// a mano a partir del centro.

void setup() {
    size(640, 360);
    noStroke();
}

void draw() {
    background(51, 51, 51);

    float w1 = mouseY / 2.0f + 10;
    fill(255, 255, 255, 204);
    rect(mouseX - w1 / 2.0f, height / 2.0f - w1 / 2.0f, w1, w1);

    int inverseX = width - mouseX;
    int inverseY = height - mouseY;
    float w2 = inverseY / 2.0f + 10;
    fill(255, 255, 255, 204);
    rect(inverseX - w2 / 2.0f, height / 2.0f - w2 / 2.0f, w2, w2);
}
