#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

SPEEX_VERSION=1.2.1

downloadTarArchive \
  "libspeex" \
  "https://ftp.osuosl.org/pub/xiph/releases/speex/speex-${SPEEX_VERSION}.tar.gz"
