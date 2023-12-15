#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

LAME_VERSION=3.100

downloadTarArchive \
  "libmp3lame" \
  "http://downloads.videolan.org/pub/contrib/lame/lame-${LAME_VERSION}.tar.gz"
