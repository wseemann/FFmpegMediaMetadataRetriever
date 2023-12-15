#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

FRIBIDI_VERSION=1.0.13

downloadTarArchive \
  "libfribidi" \
  "https://github.com/fribidi/fribidi/releases/download/v${FRIBIDI_VERSION}/fribidi-${FRIBIDI_VERSION}.tar.xz"
