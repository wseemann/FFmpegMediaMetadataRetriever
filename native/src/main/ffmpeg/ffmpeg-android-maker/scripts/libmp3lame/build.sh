#!/usr/bin/env bash

./configure \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --with-sysroot=${SYSROOT_PATH} \
    --disable-shared \
    --enable-static \
    --with-pic \
    --disable-fast-install \
    --disable-analyzer-hooks \
    --disable-gtktest \
    --disable-frontend \
    CC=${FAM_CC} \
    AR=${FAM_AR} \
    RANLIB=${FAM_RANLIB}

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lmp3lame"

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
