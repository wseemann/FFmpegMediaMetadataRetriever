#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

LIBX265_VERSION=3.5

downloadTarArchive \
  "libx265" \
  "https://bitbucket.org/multicoreware/x265_git/downloads/x265_${LIBX265_VERSION}.tar.gz"