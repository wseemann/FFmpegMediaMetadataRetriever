#!/usr/bin/env bash

# Function that downloads an archive with the source code by the given url,
# extracts its files and exports a variable SOURCES_DIR_${LIBRARY_NAME}
function downloadTarArchive() {
  # The full name of the library
  LIBRARY_NAME=$1
  # The url of the source code archive
  DOWNLOAD_URL=$2
  # Optional. If 'true' then the function creates an extra directory for archive extraction.
  NEED_EXTRA_DIRECTORY=$3

  ARCHIVE_NAME=${DOWNLOAD_URL##*/}
  # File name without extension
  LIBRARY_SOURCES="${ARCHIVE_NAME%.tar.*}"

  echo "Ensuring sources of ${LIBRARY_NAME} in ${LIBRARY_SOURCES}"

  if [[ ! -d "$LIBRARY_SOURCES" ]]; then
    curl -LO ${DOWNLOAD_URL}

    EXTRACTION_DIR="."
    if [ "$NEED_EXTRA_DIRECTORY" = true ] ; then
      EXTRACTION_DIR=${LIBRARY_SOURCES}
      mkdir ${EXTRACTION_DIR}
    fi

    tar xf ${ARCHIVE_NAME} -C ${EXTRACTION_DIR}
    rm ${ARCHIVE_NAME}
  fi

  export SOURCES_DIR_${LIBRARY_NAME}=$(pwd)/${LIBRARY_SOURCES}
}
