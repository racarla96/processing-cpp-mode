// Window.cpp
//
// Creación de ventana y bucle principal sobre SDL2 (Fase 0: usa el
// renderer 2D de SDL2 en vez de OpenGL puro, para validar el flujo
// completo con el mínimo boilerplate; ver internal.h/Renderer.cpp. Migrar
// a OpenGL puro, si hace falta para 3D, queda para Fase 1/5).

#include "processing/Sketch.h"
#include "processing/Input.h"
#include "internal.h"

#include <SDL.h>

namespace processing {

int width = 100;
int height = 100;
int frameCount = 0;

int mouseX = 0;
int mouseY = 0;
bool mousePressed = false;
bool keyPressed = false;
char key = 0;

namespace internal {
SDL_Renderer* renderer = nullptr;
} // namespace internal

void size(int w, int h) {
    width = w;
    height = h;
}

int run(int argc, char** argv) {
    (void)argc;
    (void)argv;

    if (SDL_Init(SDL_INIT_VIDEO) != 0) {
        SDL_Log("SDL_Init falló: %s", SDL_GetError());
        return 1;
    }

    // setup() puede llamar a size() para fijar el tamaño real antes de
    // crear la ventana.
    ::setup();

    SDL_Window* window = SDL_CreateWindow(
        "Processing C++ sketch",
        SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED,
        width, height,
        SDL_WINDOW_SHOWN);

    if (!window) {
        SDL_Log("SDL_CreateWindow falló: %s", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    internal::renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (!internal::renderer) {
        internal::renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_SOFTWARE);
    }
    if (!internal::renderer) {
        SDL_Log("SDL_CreateRenderer falló: %s", SDL_GetError());
        SDL_DestroyWindow(window);
        SDL_Quit();
        return 1;
    }

    bool running = true;
    while (running) {
        SDL_Event e;
        while (SDL_PollEvent(&e)) {
            switch (e.type) {
                case SDL_QUIT:
                    running = false;
                    break;
                case SDL_KEYDOWN:
                    keyPressed = true;
                    key = (char)e.key.keysym.sym;
                    if (e.key.keysym.sym == SDLK_ESCAPE) {
                        running = false;
                    }
                    break;
                case SDL_KEYUP:
                    keyPressed = false;
                    break;
                case SDL_MOUSEMOTION:
                    mouseX = e.motion.x;
                    mouseY = e.motion.y;
                    break;
                case SDL_MOUSEBUTTONDOWN:
                    mousePressed = true;
                    break;
                case SDL_MOUSEBUTTONUP:
                    mousePressed = false;
                    break;
                default:
                    break;
            }
        }

        ::draw();
        SDL_RenderPresent(internal::renderer);
        frameCount++;

        // ~60 FPS fijos. framerate() configurable y un reloj real llegan
        // en Fase 1.
        SDL_Delay(16);
    }

    SDL_DestroyRenderer(internal::renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}

} // namespace processing
