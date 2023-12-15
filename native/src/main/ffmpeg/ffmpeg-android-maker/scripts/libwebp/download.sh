#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

WEBP_VERSION=1.3.2

downloadTarArchive \
  "libwebp" \
  "https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-${WEBP_VERSION}.tar.gz"
