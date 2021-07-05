#!/usr/bin/env bash

# Defining a toolchain directory's name according to the current OS.
# Assume that proper version of NDK is installed
# and is referenced by ANDROID_NDK_HOME environment variable
case "$OSTYPE" in
  darwin*)  HOST_TAG="darwin-x86_64" ;;
  linux*)   HOST_TAG="linux-x86_64" ;;
  msys)
    case "$(uname -m)" in
      x86_64) HOST_TAG="windows-x86_64" ;;
      i686)   HOST_TAG="windows" ;;
    esac
  ;;
esac

if [[ $OSTYPE == "darwin"* ]]; then
  HOST_NPROC=$(sysctl -n hw.physicalcpu)
else
  HOST_NPROC=$(nproc)
fi

# The variable is used as a path segment of the toolchain path
export HOST_TAG=$HOST_TAG
# Number of physical cores in the system to facilitate parallel assembling
export HOST_NPROC=$HOST_NPROC

# Using CMake from the Android SDK
export CMAKE_EXECUTABLE=${ANDROID_SDK_HOME}/cmake/3.10.2.4988404/bin/cmake
# Using Build machine's Make, because Android NDK's Make (before r21) doesn't work properly in MSYS2 on Windows
export MAKE_EXECUTABLE=$(which make)
# Using Build machine's Ninja. It is used for libdav1d building. Needs to be installed
export NINJA_EXECUTABLE=$(which ninja)
# Meson is used for libdav1d building. Needs to be installed
export MESON_EXECUTABLE=$(which meson)
# Nasm is used for libdav1d building. Needs to be installed
export NASM_EXECUTABLE=$(which nasm)
