#!/usr/bin/env bash

ADDITIONAL_FLAGS=
if [[ $ANDROID_ABI = "x86" ]] || [[ $ANDROID_ABI = "x86_64" ]]; then
  ADDITIONAL_FLAGS=--enable-sse
fi

./configure \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --with-sysroot=${SYSROOT_PATH} \
    --disable-shared \
    --enable-static \
    --with-pic \
    CC=${FAM_CC} \
    AR=${FAM_AR} \
    RANLIB=${FAM_RANLIB} \
    ${ADDITIONAL_FLAGS} || exit 1

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lm"

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
