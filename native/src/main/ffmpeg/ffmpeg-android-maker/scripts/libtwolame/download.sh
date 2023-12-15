#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

TWOLAME_VERSION=0.4.0

downloadTarArchive \
  "libtwolame" \
  "https://netix.dl.sourceforge.net/project/twolame/twolame/${TWOLAME_VERSION}/twolame-${TWOLAME_VERSION}.tar.gz"
