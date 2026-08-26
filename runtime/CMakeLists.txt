# CMakeLists.txt
#
# Build del runtime (equivalente a core.jar) como librería estática/objeto,
# sobre SDL2/OpenGL.
#
# TODO (Fase 1 - Runtime C++): implementar.

cmake_minimum_required(VERSION 3.16)
project(processing_cpp_runtime CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# TODO: add_library(processing_cpp_runtime STATIC ...) + find_package(SDL2/OpenGL)
