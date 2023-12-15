#!/usr/bin/env bash

source ${SCRIPTS_DIR}/common-functions.sh

export MBEDTLS_VERSION=3.5.1
downloadTarArchive \
  "mbedtls" \
  "https://github.com/Mbed-TLS/mbedtls/archive/refs/tags/v${MBEDTLS_VERSION}.tar.gz" \
  true