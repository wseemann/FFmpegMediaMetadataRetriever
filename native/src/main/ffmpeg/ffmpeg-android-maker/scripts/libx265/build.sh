#!/usr/bin/env bash

# libaom doesn't support building while being in its root directory
CMAKE_BUILD_DIR=libx265_build_${ANDROID_ABI}
rm -rf ${CMAKE_BUILD_DIR}
mkdir ${CMAKE_BUILD_DIR}
cd ${CMAKE_BUILD_DIR}

EXTRA_CMAKE_ARG=""
case $ANDROID_ABI in
  arm64-v8a|x86)
    # Disabling assembler optimizations for certain ABIs. Not a good solution, but it at least works
    EXTRA_CMAKE_ARG="-DENABLE_ASSEMBLY=OFF"
    ;;
esac

${CMAKE_EXECUTABLE} ../source \
 -DANDROID_PLATFORM=${ANDROID_PLATFORM} \
 -DANDROID_ABI=${ANDROID_ABI} \
 -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK_HOME}/build/cmake/android.toolchain.cmake \
 -DCMAKE_INSTALL_PREFIX=${INSTALL_DIR} \
 -DENABLE_PIC=ON \
 -DENABLE_SHARED=OFF \
 -DENABLE_CLI=OFF \
 $EXTRA_CMAKE_ARG

EXTRA_SED_ARG=""
if [[ "$OSTYPE" == "darwin"* ]]; then
  EXTRA_SED_ARG="''"
fi
sed -i $EXTRA_SED_ARG 's/-lpthread/-pthread/' CMakeFiles/cli.dir/link.txt
sed -i $EXTRA_SED_ARG 's/-lpthread/-pthread/' CMakeFiles/x265-shared.dir/link.txt
sed -i $EXTRA_SED_ARG 's/-lpthread/-pthread/' CMakeFiles/x265-static.dir/link.txt

export FFMPEG_EXTRA_LD_FLAGS="${FFMPEG_EXTRA_LD_FLAGS} -lm -lc++"

${MAKE_EXECUTABLE} -j${HOST_NPROC}
${MAKE_EXECUTABLE} install
