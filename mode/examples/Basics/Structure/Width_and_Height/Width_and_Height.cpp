// Width and Height.
//
// Adaptado del ejemplo clásico de Processing. Las variables width y
// height contienen el ancho y alto de la ventana definidos en size().

void setup() {
    size(640, 360);
}

void draw() {
    background(127, 127, 127);
    noStroke();
    for (int i = 0; i < height; i += 20) {
        fill(129, 206, 15);
        rect(0, i, width, 10);
        fill(255, 255, 255);
        rect(i, 0, 10, height);
    }
}
