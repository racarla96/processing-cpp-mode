#pragma once

// Una pelota que rebota dentro de la ventana. Declarada en un .hpp
// aparte del sketch principal, para probar que el Mode soporta tabs
// auxiliares .hpp/.cpp además del .cpp principal (no solo funciones
// sueltas en una única tab).

class Ball {
public:
    Ball(float startX, float startY, float startDiameter);

    void update();
    void display();

private:
    float x, y;
    float diameter;
    float speedX, speedY;
};
