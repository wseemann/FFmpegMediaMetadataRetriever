#!/usr/bin/env bash

# The Wavpack may reqire libiconv for proper work
# Consider building it and passing via --with-iconv

if [[ $ANDROID_ABI = "arm64-v8a" ]]; then
  ADDITIONAL_FLAGS=--disable-asm
fi

./configure \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --with-sysroot=${SYSROOT_PATH} \
    --disable-shared \
    --enable-static \
    --with-pic \
    --disable-apps \
    --disable-tests \
    --disable-man \
    CC=${FAM_CC} \
    AR=${FAM_AR} \
    RANLIB=${FAM_RANLIB} \
    ${ADDITIONAL_FLAGS} || exit 1

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lm"

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
