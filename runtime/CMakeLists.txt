# CMakeLists.txt
#
# Build del runtime (equivalente a core.jar) como librería estática sobre
# SDL2. Ver Window.cpp para la nota sobre por qué SDL2 2D en vez de
# OpenGL puro en esta fase.

cmake_minimum_required(VERSION 3.16)
project(processing_cpp_runtime CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(SDL2 REQUIRED)

add_library(processing_cpp_runtime STATIC
    src/Window.cpp
    src/Renderer.cpp
)

target_include_directories(processing_cpp_runtime
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_link_libraries(processing_cpp_runtime
    PUBLIC SDL2::SDL2
)
