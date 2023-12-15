#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

# Libx264 doesn't have any versioning system. Currently it has 2 branches: master and stable.
# Latest commit in stable branch
# Oct 1, 2023 4:28pm GMT
LIBX264_VERSION=31e19f92f00c7003fa115047ce50978bc98c3a0d

downloadTarArchive \
  "libx264" \
  "https://code.videolan.org/videolan/x264/-/archive/${LIBX264_VERSION}/x264-${LIBX264_VERSION}.tar.gz"
