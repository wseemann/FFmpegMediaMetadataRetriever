#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

WAVPACK_VERSION=5.6.0

downloadTarArchive \
  "libwavpack" \
  "http://www.wavpack.com/wavpack-${WAVPACK_VERSION}.tar.bz2"
