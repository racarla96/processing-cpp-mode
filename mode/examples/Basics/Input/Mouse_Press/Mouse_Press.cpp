// Mouse Press.
//
// Adaptado del ejemplo clásico de Processing. Mover el ratón coloca la
// cruz; pulsar el botón invierte el color. El original no llama a
// background() dentro de draw() (deja rastro sobre el frame anterior);
// nuestro renderer usa un back buffer con doble búfer, así que sin
// limpiar cada frame el resultado parpadea — se llama a background()
// en cada draw() para mantenerlo estable (queda pendiente soportar
// dibujo persistente de verdad más adelante).

void setup() {
    size(640, 360);
}

void draw() {
    background(102, 102, 102);

    if (mousePressed) {
        stroke(255, 255, 255);
    } else {
        stroke(0, 0, 0);
    }

    line(mouseX - 66, mouseY, mouseX + 66, mouseY);
    line(mouseX, mouseY - 66, mouseX, mouseY + 66);
}
