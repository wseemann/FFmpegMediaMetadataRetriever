#!/usr/bin/env bash

./configure \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --disable-shared \
    --enable-static \
    --disable-fast-install \
    --with-pic \
    --with-sysroot=${SYSROOT_PATH} \
    --enable-asm \
    --enable-check-asm \
    --disable-rtcd \
    --disable-doc \
    --disable-extra-programs \
    CC=${FAM_CC} \
    CCLD=${FAM_LD} \
    CCAS=${FAM_AS} \
    RANLIB=${FAM_RANLIB} \
    AR=${FAM_AR}

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lm"

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
