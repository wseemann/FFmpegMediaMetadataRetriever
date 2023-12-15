#!/usr/bin/env bash

CROSS_FILE_NAME=crossfile-${ANDROID_ABI}.meson

rm ${CROSS_FILE_NAME}

cat > "${CROSS_FILE_NAME}" << EOF
[binaries]
c = '${FAM_CC}'
ar = '${FAM_AR}'
strip = '${FAM_STRIP}'
nasm = '${NASM_EXECUTABLE}'
pkgconfig = '${PKG_CONFIG_EXECUTABLE}'

[properties]
needs_exe_wrapper = true
sys_root = '${SYSROOT_PATH}'

[host_machine]
system = 'linux'
cpu_family = '${CPU_FAMILY}'
cpu = '${TARGET_TRIPLE_MACHINE_ARCH}'
endian = 'little'

[built-in options]
prefix = '${INSTALL_DIR}'
EOF

BUILD_DIRECTORY=build/${ANDROID_ABI}

rm -rf ${BUILD_DIRECTORY}

${MESON_EXECUTABLE} . ${BUILD_DIRECTORY} \
  --cross-file ${CROSS_FILE_NAME} \
  --default-library=static \
  -Denable_asm=true \
  -Denable_tools=false \
  -Denable_tests=false \
  -Denable_examples=false \
  -Dtestdata_tests=false

cd ${BUILD_DIRECTORY}

${NINJA_EXECUTABLE} -j ${HOST_NPROC}
${NINJA_EXECUTABLE} install
