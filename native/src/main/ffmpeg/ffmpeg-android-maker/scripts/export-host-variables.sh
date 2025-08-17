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
export CMAKE_EXECUTABLE=$(which cmake)
# Using Make from the Android SDK
export MAKE_EXECUTABLE=${ANDROID_NDK_HOME}/prebuilt/${HOST_TAG}/bin/make
# Using Build machine's Ninja. It is used for libdav1d building. Needs to be installed
export NINJA_EXECUTABLE=$(which ninja)
# Meson is used for libdav1d building. Needs to be installed
export MESON_EXECUTABLE=$(which meson)
# Nasm is used for libdav1d and libx264 building. Needs to be installed
export NASM_EXECUTABLE=$(which nasm)
# A utility to properly pick shared libraries by FFmpeg's configure script. Needs to be installed
export PKG_CONFIG_EXECUTABLE=$(which pkg-config)
