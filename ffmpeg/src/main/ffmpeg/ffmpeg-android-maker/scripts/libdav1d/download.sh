#!/usr/bin/env bash

# Script to download Dav1d's source code

# Exports SOURCES_DIR_libdav1d - path where actual sources are stored

DAV1D_VERSION=0.5.2
echo "Using libdav1d $DAV1D_VERSION"
DAV1D_SOURCES=dav1d-${DAV1D_VERSION}

if [[ ! -d "$DAV1D_SOURCES" ]]; then
  TARGET_FILE_NAME=dav1d-${DAV1D_VERSION}.tar.gz

  curl https://code.videolan.org/videolan/dav1d/-/archive/${DAV1D_VERSION}/dav1d-${DAV1D_VERSION}.tar.gz --output ${TARGET_FILE_NAME}
  tar xf ${TARGET_FILE_NAME} -C .
  rm ${TARGET_FILE_NAME}
fi

export SOURCES_DIR_libdav1d=$(pwd)/${DAV1D_SOURCES}
