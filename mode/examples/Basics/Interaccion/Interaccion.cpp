// Interaccion
//
// Input básico: mouseX/mouseY, pmouseX/pmouseY, mousePressed.

void setup() {
    size(640, 360);
    frameRate(60);
}

void draw() {
    background(30, 30, 40);

    // Rastro entre la posición del ratón en el frame anterior y la actual.
    stroke(255, 220, 60);
    line((float)pmouseX, (float)pmouseY, (float)mouseX, (float)mouseY);

    // El círculo se vacía mientras se mantiene pulsado el ratón.
    stroke(255);
    if (mousePressed) {
        noFill();
    } else {
        fill(220, 60, 60);
    }
    ellipse((float)mouseX, (float)mouseY, 80, 80);
}
