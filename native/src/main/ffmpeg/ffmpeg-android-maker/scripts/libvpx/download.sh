#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

VPX_VERSION=v1.15.1

downloadTarArchive \
  "libvpx" \
  "https://chromium.googlesource.com/webm/libvpx/+archive/${VPX_VERSION}.tar.gz" \
  true
