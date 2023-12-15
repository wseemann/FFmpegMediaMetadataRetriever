#!/usr/bin/env bash

VPX_AS=${FAM_AS}

case $ANDROID_ABI in
  x86)
    EXTRA_BUILD_FLAGS="--target=x86-android-gcc --disable-sse4_1 --disable-avx --disable-avx2 --disable-avx512"
    VPX_AS=${FAM_YASM}
    ;;
  x86_64)
    EXTRA_BUILD_FLAGS="--target=x86_64-android-gcc --disable-avx --disable-avx2 --disable-avx512"
    VPX_AS=${FAM_YASM}
    ;;
  armeabi-v7a)
    EXTRA_BUILD_FLAGS="--target=armv7-android-gcc --enable-thumb --disable-neon"
    ;;
  arm64-v8a)
    EXTRA_BUILD_FLAGS="--target=arm64-android-gcc --enable-thumb"
    ;;
esac

CC=${FAM_CC} \
CXX=${FAM_CXX} \
AR=${FAM_AR} \
LD=${FAM_LD} \
AS=${VPX_AS} \
STRIP=${FAM_STRIP} \
NM=${FAM_NM} \
./configure \
    ${EXTRA_BUILD_FLAGS} \
    --prefix=${INSTALL_DIR} \
    --libc=${SYSROOT_PATH} \
    --enable-pic \
    --enable-realtime-only \
    --enable-install-libs \
    --enable-multithread \
    --enable-webm-io \
    --enable-libyuv \
    --enable-small \
    --enable-better-hw-compatibility \
    --enable-vp8 \
    --enable-vp9 \
    --enable-static \
    --disable-shared \
    --disable-ccache \
    --disable-debug \
    --disable-gprof \
    --disable-gcov \
    --disable-dependency-tracking \
    --disable-install-docs \
    --disable-install-bins \
    --disable-install-srcs \
    --disable-examples \
    --disable-tools \
    --disable-docs \
    --disable-unit-tests \
    --disable-decode-perf-tests \
    --disable-encode-perf-tests \
    --disable-runtime-cpu-detect  || exit 1

${MAKE_EXECUTABLE} clean
${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
