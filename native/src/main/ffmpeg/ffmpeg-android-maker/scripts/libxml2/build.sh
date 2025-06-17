#!/usr/bin/env bash

./autogen.sh \
    --prefix=${INSTALL_DIR} \
    --host=${TARGET} \
    --with-sysroot=${SYSROOT_PATH} \
    --disable-shared \
    --disable-fast-install \
    --enable-static \
    --with-zlib \
    --with-pic \
    --without-python \
    --without-debug \
    --without-lzma \
    CC=${FAM_CC} \
    AR=${FAM_AR} \
    RANLIB=${FAM_RANLIB} || exit 1

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lm -lz"

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
