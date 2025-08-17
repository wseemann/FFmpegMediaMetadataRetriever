#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

TWOLAME_VERSION=0.4.0

downloadTarArchive \
  "libtwolame" \
  "https://downloads.videolan.org/pub/contrib/twolame/twolame-${TWOLAME_VERSION}.tar.gz"
