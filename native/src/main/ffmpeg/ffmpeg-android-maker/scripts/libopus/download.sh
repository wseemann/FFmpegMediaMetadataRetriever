#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

OPUS_VERSION=1.5.2

downloadTarArchive \
  "libopus" \
  "https://downloads.xiph.org/releases/opus/opus-${OPUS_VERSION}.tar.gz"
