// Color.h
//
// background()/fill()/stroke() mínimos (Fase 0): valores RGBA en
// [0, 255], sin modos de color alternativos todavía (ver Fase 5).

#pragma once

namespace processing {

// Limpia la ventana con el color dado. Debe llamarse al principio de
// draw() (igual que en Processing).
void background(int r, int g, int b, int a = 255);

// Color de relleno usado por las siguientes llamadas a rect()/ellipse().
void fill(int r, int g, int b, int a = 255);

// Color de contorno usado por rect()/ellipse()/line().
void stroke(int r, int g, int b, int a = 255);

} // namespace processing
