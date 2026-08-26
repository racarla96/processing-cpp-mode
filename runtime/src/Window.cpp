// Window.cpp
//
// Creación de ventana y bucle principal sobre SDL2 (usa el renderer 2D
// de SDL2 en vez de OpenGL puro, para minimizar boilerplate; ver
// internal.h/Renderer.cpp. Migrar a OpenGL puro, si hace falta para 3D,
// queda para una fase posterior).

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
int pmouseX = 0;
int pmouseY = 0;
bool mousePressed = false;
int mouseButton = 0;
bool keyPressed = false;
char key = 0;

namespace internal {
SDL_Renderer* renderer = nullptr;
float targetFrameRate = 60.0f;
} // namespace internal

void size(int w, int h) {
    width = w;
    height = h;
}

void frameRate(float fps) {
    if (fps > 0.0f) {
        internal::targetFrameRate = fps;
    }
}

int run(int argc, char** argv) {
    (void)argc;
    (void)argv;

    if (SDL_Init(SDL_INIT_VIDEO) != 0) {
        SDL_Log("SDL_Init falló: %s", SDL_GetError());
        return 1;
    }

    // setup() puede llamar a size() y frameRate() para fijar el tamaño
    // real y el framerate objetivo antes de crear la ventana.
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

    const Uint64 perfFreq = SDL_GetPerformanceFrequency();

    bool running = true;
    while (running) {
        const Uint64 frameStart = SDL_GetPerformanceCounter();

        // pmouseX/pmouseY reflejan la posición al inicio del frame
        // anterior, igual que en Processing; mouseX/mouseY se van
        // actualizando con cada evento de este frame.
        pmouseX = mouseX;
        pmouseY = mouseY;

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
                    mouseButton = e.button.button; // coincide con LEFT/CENTER/RIGHT
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

        const Uint64 frameEnd = SDL_GetPerformanceCounter();
        const double elapsedMs = (double)(frameEnd - frameStart) * 1000.0 / (double)perfFreq;
        const double targetMs = 1000.0 / (double)internal::targetFrameRate;
        if (elapsedMs < targetMs) {
            SDL_Delay((Uint32)(targetMs - elapsedMs));
        }
    }

    SDL_DestroyRenderer(internal::renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}

} // namespace processing
