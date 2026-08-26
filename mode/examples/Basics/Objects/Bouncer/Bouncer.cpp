// Bouncer.
//
// Una clase (Ball) declarada en Ball.hpp e implementada en Ball.cpp,
// usada desde el sketch principal. Demuestra que el Mode soporta tabs
// auxiliares .hpp/.cpp, no solo el .cpp principal.

#include "Ball.hpp"

Ball ball(320, 180, 60);

void setup() {
    size(640, 360);
}

void draw() {
    background(30, 30, 40);
    ball.update();
    ball.display();
}
