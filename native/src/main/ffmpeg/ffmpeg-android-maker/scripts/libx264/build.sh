#!/usr/bin/env bash

X264_AS=${FAM_CC}

X264_ADDITIONAL_FLAGS=

case $ANDROID_ABI in
  x86)
    # Disabling assembler optimizations due to text relocations
    X264_ADDITIONAL_FLAGS=--disable-asm
    ;;
  x86_64)
    X264_AS=${NASM_EXECUTABLE}
    ;;
esac

CC=${FAM_CC} \
AR=${FAM_AR} \
AS=${X264_AS} \
RANLIB=${FAM_RANLIB} \
STRIP=${FAM_STRIP} \
./configure \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --sysroot=${SYSROOT_PATH} \
    --enable-pic \
    --enable-static \
    --disable-cli \
    --disable-avs \
    --disable-lavf \
    --disable-cli \
    --disable-ffms \
    --disable-opencl \
    --chroma-format=all \
    --bit-depth=all \
    ${X264_ADDITIONAL_FLAGS} || exit 1

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
