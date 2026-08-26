# CMakeLists.txt (raíz)
#
# Build de validación técnica (Fase 0): compila el runtime y el ejemplo
# examples/hello_circle a mano, sin pasar por el Mode/PDE todavía.
#
#   cmake -B build
#   cmake --build build
#   ./build/examples/hello_circle/hello_circle

cmake_minimum_required(VERSION 3.16)
project(processing_cpp_mode CXX)

add_subdirectory(runtime)
add_subdirectory(examples/hello_circle)
