// Renderer.cpp
//
// Implementación de las primitivas declaradas en include/processing/
// Shapes.h y Color.h, sobre el SDL_Renderer creado en Window.cpp (ver
// nota de Fase 0 en Window.cpp sobre por qué SDL2 2D en vez de OpenGL
// puro).

#include "processing/Color.h"
#include "processing/Shapes.h"
#include "internal.h"

#include <SDL.h>
#include <cmath>

namespace processing {

namespace internal {
RGBA fillColor{255, 255, 255, 255};
RGBA strokeColor{0, 0, 0, 255};
} // namespace internal

void background(int r, int g, int b, int a) {
    SDL_SetRenderDrawColor(internal::renderer, (Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a);
    SDL_RenderClear(internal::renderer);
}

void fill(int r, int g, int b, int a) {
    internal::fillColor = {(Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a};
}

void stroke(int r, int g, int b, int a) {
    internal::strokeColor = {(Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a};
}

void rect(float x, float y, float w, float h) {
    SDL_FRect r{x, y, w, h};

    const auto& fc = internal::fillColor;
    SDL_SetRenderDrawColor(internal::renderer, fc.r, fc.g, fc.b, fc.a);
    SDL_RenderFillRectF(internal::renderer, &r);

    const auto& sc = internal::strokeColor;
    SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
    SDL_RenderDrawRectF(internal::renderer, &r);
}

void line(float x1, float y1, float x2, float y2) {
    const auto& sc = internal::strokeColor;
    SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
    SDL_RenderDrawLineF(internal::renderer, x1, y1, x2, y2);
}

// (x, y) es el centro; w/h son diámetros, igual que en Processing
// (modo CENTER, el default). Relleno por scanlines horizontales; el
// contorno queda pendiente para Fase 1 (por ahora solo se rellena).
void ellipse(float x, float y, float w, float h) {
    float rx = w / 2.0f;
    float ry = h / 2.0f;
    if (rx <= 0.0f || ry <= 0.0f) {
        return;
    }

    const auto& fc = internal::fillColor;
    SDL_SetRenderDrawColor(internal::renderer, fc.r, fc.g, fc.b, fc.a);

    int dyMax = (int)std::ceil(ry);
    for (int dy = -dyMax; dy <= dyMax; ++dy) {
        float t = 1.0f - (dy * dy) / (ry * ry);
        if (t < 0.0f) {
            continue;
        }
        float dx = rx * std::sqrt(t);
        SDL_RenderDrawLineF(internal::renderer, x - dx, y + dy, x + dx, y + dy);
    }
}

} // namespace processing
