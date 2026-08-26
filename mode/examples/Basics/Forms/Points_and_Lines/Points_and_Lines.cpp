// Points and Lines.
//
// Adaptado del ejemplo clásico de Processing. Puntos y líneas para
// dibujar geometría básica; cambiar 'd' escala la figura. El original
// corre en "modo estático" (sin setup()/draw()) y usa translate(140,0);
// aquí se envuelve en draw() y el desplazamiento se suma a mano a cada
// coordenada.

void setup() {
    size(640, 360);
}

void draw() {
    background(0, 0, 0);

    int d = 70;
    int p1 = d;
    int p2 = p1 + d;
    int p3 = p2 + d;
    int p4 = p3 + d;
    int offsetX = 140; // equivalente a translate(140, 0)

    // Caja gris.
    stroke(153, 153, 153);
    line(offsetX + p3, p3, offsetX + p2, p3);
    line(offsetX + p2, p3, offsetX + p2, p2);
    line(offsetX + p2, p2, offsetX + p3, p2);
    line(offsetX + p3, p2, offsetX + p3, p3);

    // Puntos blancos.
    stroke(255, 255, 255);
    point(offsetX + p1, p1);
    point(offsetX + p1, p3);
    point(offsetX + p2, p4);
    point(offsetX + p3, p1);
    point(offsetX + p4, p2);
    point(offsetX + p4, p4);
}
