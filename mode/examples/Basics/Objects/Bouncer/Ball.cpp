#include "Ball.hpp"

// Implementación de Ball. Como es una tab .cpp, se concatena en el
// mismo archivo que Bouncer.cpp (ver CppSketch.java), así que también
// tiene acceso directo a la API de Processing (width, height, fill(),
// ellipse()...) sin prefijo.

Ball::Ball(float startX, float startY, float startDiameter)
    : x(startX), y(startY), diameter(startDiameter), speedX(2.5f), speedY(2.0f) {
}

void Ball::update() {
    x += speedX;
    y += speedY;

    if (x - diameter / 2 < 0 || x + diameter / 2 > width) {
        speedX *= -1;
    }
    if (y - diameter / 2 < 0 || y + diameter / 2 > height) {
        speedY *= -1;
    }
}

void Ball::display() {
    noStroke();
    fill(220, 60, 60);
    ellipse(x, y, diameter, diameter);
}
