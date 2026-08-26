// Sketch.h
//
// Clase base con setup()/draw(), equivalente a PApplet. Es el primer
// archivo a escribir en la Fase 0 (Validación técnica): debe ser mínimo
// y permitir compilar y correr un ejemplo a mano antes de tocar Java/PDE.
//
// TODO (Fase 0 - Validación técnica): implementar versión mínima.

#pragma once

namespace processing {

class Sketch {
public:
    virtual ~Sketch() = default;
    virtual void setup() {}
    virtual void draw() {}
};

} // namespace processing
