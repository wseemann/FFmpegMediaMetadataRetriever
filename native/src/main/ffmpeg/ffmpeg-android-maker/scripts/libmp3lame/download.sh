#!/usr/bin/env bash

# Script to download Lame's source code

# Exports SOURCES_DIR_libmp3lame - path where actual sources are stored

LAME_VERSION=3.100
echo "Using libmp3lame $LAME_VERSION"
LAME_SOURCES=lame-${LAME_VERSION}

if [[ ! -d "$LAME_SOURCES" ]]; then
  TARGET_FILE_NAME=lame-${LAME_VERSION}.tar.gz

  curl http://downloads.videolan.org/pub/contrib/lame/${TARGET_FILE_NAME} --output ${TARGET_FILE_NAME}
  tar xf ${TARGET_FILE_NAME} -C .
  rm ${TARGET_FILE_NAME}
fi

export SOURCES_DIR_libmp3lame=$(pwd)/${LAME_SOURCES}
