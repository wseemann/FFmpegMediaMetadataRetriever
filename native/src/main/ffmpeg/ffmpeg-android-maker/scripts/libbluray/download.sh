#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

LIBBLURAY_VERSION=1.3.4

downloadTarArchive \
  "libbluray" \
  "https://download.videolan.org/pub/videolan/libbluray/${LIBBLURAY_VERSION}/libbluray-${LIBBLURAY_VERSION}.tar.bz2"
  
