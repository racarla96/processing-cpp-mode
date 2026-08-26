// Shapes.h
//
// Primitivas 2D. x,y en rect() es la esquina superior izquierda (modo
// CORNER, el default de Processing); x,y en ellipse() es el centro
// (modo CENTER, también el default). w,h son ancho/alto (o diámetros,
// en el caso de ellipse()). Todas respetan noFill()/noStroke(), salvo
// point(), que siempre se dibuja con el color de stroke.

#pragma once

namespace processing {

void point(float x, float y);
void line(float x1, float y1, float x2, float y2);
void rect(float x, float y, float w, float h);
void ellipse(float x, float y, float w, float h);
void triangle(float x1, float y1, float x2, float y2, float x3, float y3);

} // namespace processing
