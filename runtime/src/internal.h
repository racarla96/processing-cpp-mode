// internal.h
//
// Estado interno compartido entre Window.cpp y Renderer.cpp. No es parte
// de la API pública (no vive bajo include/processing/): el sketch del
// usuario nunca debe incluir este header.

#pragma once

#include <SDL.h>

namespace processing {
namespace internal {

// SDL_Renderer de la ventana activa, creado en run() (Window.cpp) y
// usado por las primitivas de dibujo (Renderer.cpp).
extern SDL_Renderer* renderer;

struct RGBA {
    Uint8 r = 0, g = 0, b = 0, a = 255;
};

extern RGBA fillColor;
extern RGBA strokeColor;

} // namespace internal
} // namespace processing
