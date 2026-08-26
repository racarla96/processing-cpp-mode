// Input.h
//
// Variables globales de entrada, actualizadas por el bucle de eventos en
// Window.cpp antes de cada llamada a draw(). pmouseX/pmouseY se fijan
// una vez por frame (al valor de mouseX/mouseY del frame anterior),
// igual que en Processing.

#pragma once

namespace processing {

extern int mouseX;
extern int mouseY;
extern int pmouseX;
extern int pmouseY;
extern bool mousePressed;

// Último botón pulsado/soltado: LEFT, CENTER o RIGHT (0 si ninguno
// todavía).
extern int mouseButton;
constexpr int LEFT = 1;
constexpr int CENTER = 2;
constexpr int RIGHT = 3;

extern bool keyPressed;
extern char key;

} // namespace processing
