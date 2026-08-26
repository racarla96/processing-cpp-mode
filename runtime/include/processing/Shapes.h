// Shapes.h
//
// Primitivas 2D mínimas (Fase 0). x,y en rect() es la esquina superior
// izquierda (modo CORNER, el default de Processing); x,y en ellipse() es
// el centro (modo CENTER, también el default). w,h son ancho/alto (o
// diámetros, en el caso de ellipse()).

#pragma once

namespace processing {

void rect(float x, float y, float w, float h);
void ellipse(float x, float y, float w, float h);
void line(float x1, float y1, float x2, float y2);

} // namespace processing
