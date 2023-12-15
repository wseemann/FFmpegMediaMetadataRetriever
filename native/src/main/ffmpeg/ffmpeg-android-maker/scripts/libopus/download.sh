#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

OPUS_VERSION=1.3.1

downloadTarArchive \
  "libopus" \
  "https://archive.mozilla.org/pub/opus/opus-${OPUS_VERSION}.tar.gz"
