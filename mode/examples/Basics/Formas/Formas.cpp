// Formas
//
// Primitivas básicas: rect(), ellipse(), triangle(), line(), point().
// noFill()/noStroke() controlan si se rellena o se contornea cada una.

void setup() {
    size(640, 360);
}

void draw() {
    background(240);

    // Rectángulo relleno, sin contorno.
    noStroke();
    fill(60, 140, 220);
    rect(40, 40, 140, 100);

    // Elipse con relleno y contorno.
    fill(220, 60, 60);
    stroke(0);
    ellipse(320, 90, 120, 120);

    // Triángulo solo con contorno (sin relleno).
    noFill();
    stroke(80, 160, 80);
    triangle(460, 160, 560, 160, 510, 40);

    // Línea y un punto.
    stroke(0);
    line(40, 200, 600, 200);
    point(320, 250);
}
