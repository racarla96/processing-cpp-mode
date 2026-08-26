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
bool doFill = true;
bool doStroke = true;
} // namespace internal

namespace {
constexpr float kTwoPi = 6.28318530717958647692f;
}

void background(int r, int g, int b, int a) {
    SDL_SetRenderDrawColor(internal::renderer, (Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a);
    SDL_RenderClear(internal::renderer);
}

void fill(int r, int g, int b, int a) {
    internal::doFill = true;
    internal::fillColor = {(Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a};
}

void stroke(int r, int g, int b, int a) {
    internal::doStroke = true;
    internal::strokeColor = {(Uint8)r, (Uint8)g, (Uint8)b, (Uint8)a};
}

void noFill() {
    internal::doFill = false;
}

void noStroke() {
    internal::doStroke = false;
}

void point(float x, float y) {
    const auto& sc = internal::strokeColor;
    SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
    SDL_RenderDrawPointF(internal::renderer, x, y);
}

void line(float x1, float y1, float x2, float y2) {
    if (!internal::doStroke) {
        return;
    }
    const auto& sc = internal::strokeColor;
    SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
    SDL_RenderDrawLineF(internal::renderer, x1, y1, x2, y2);
}

void rect(float x, float y, float w, float h) {
    SDL_FRect r{x, y, w, h};

    if (internal::doFill) {
        const auto& fc = internal::fillColor;
        SDL_SetRenderDrawColor(internal::renderer, fc.r, fc.g, fc.b, fc.a);
        SDL_RenderFillRectF(internal::renderer, &r);
    }
    if (internal::doStroke) {
        const auto& sc = internal::strokeColor;
        SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
        SDL_RenderDrawRectF(internal::renderer, &r);
    }
}

// (x, y) es el centro; w/h son diámetros, igual que en Processing
// (modo CENTER, el default). Relleno por scanlines horizontales;
// contorno como polígono de segmentos fijos.
void ellipse(float x, float y, float w, float h) {
    float rx = w / 2.0f;
    float ry = h / 2.0f;
    if (rx <= 0.0f || ry <= 0.0f) {
        return;
    }

    if (internal::doFill) {
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

    if (internal::doStroke) {
        const auto& sc = internal::strokeColor;
        SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);

        constexpr int kSegments = 48;
        SDL_FPoint pts[kSegments + 1];
        for (int i = 0; i <= kSegments; ++i) {
            float t = kTwoPi * (float)i / (float)kSegments;
            pts[i] = {x + rx * std::cos(t), y + ry * std::sin(t)};
        }
        SDL_RenderDrawLinesF(internal::renderer, pts, kSegments + 1);
    }
}

void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
    if (internal::doFill) {
        const auto& fc = internal::fillColor;
        SDL_Color c{fc.r, fc.g, fc.b, fc.a};
        SDL_Vertex verts[3] = {
            {{x1, y1}, c, {0, 0}},
            {{x2, y2}, c, {0, 0}},
            {{x3, y3}, c, {0, 0}},
        };
        SDL_RenderGeometry(internal::renderer, nullptr, verts, 3, nullptr, 0);
    }

    if (internal::doStroke) {
        const auto& sc = internal::strokeColor;
        SDL_SetRenderDrawColor(internal::renderer, sc.r, sc.g, sc.b, sc.a);
        SDL_FPoint pts[4] = {{x1, y1}, {x2, y2}, {x3, y3}, {x1, y1}};
        SDL_RenderDrawLinesF(internal::renderer, pts, 4);
    }
}

} // namespace processing
