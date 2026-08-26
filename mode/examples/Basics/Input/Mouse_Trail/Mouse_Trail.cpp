// Mouse Trail.
//
// Ejemplo propio (no viene de Processing clásico): pmouseX/pmouseY
// (posición del ratón en el frame anterior), noFill()/noStroke(), y
// frameRate(). El círculo se vacía mientras se mantiene pulsado el
// ratón.

void setup() {
    size(640, 360);
    frameRate(60);
}

void draw() {
    background(30, 30, 40);

    stroke(255, 220, 60);
    line(pmouseX, pmouseY, mouseX, mouseY);

    stroke(255, 255, 255);
    if (mousePressed) {
        noFill();
    } else {
        fill(220, 60, 60);
    }
    ellipse(mouseX, mouseY, 80, 80);
}
