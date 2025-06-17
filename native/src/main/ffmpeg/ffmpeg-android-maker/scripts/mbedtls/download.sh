#!/usr/bin/env bash

MBEDTLS_VERSION=v3.6.3

git clone \
 --depth 1 \
 --branch $MBEDTLS_VERSION \
 --recursive \
 https://github.com/Mbed-TLS/mbedtls.git \
 $MBEDTLS_VERSION

LIBRARY_NAME=mbedtls
export SOURCES_DIR_${LIBRARY_NAME}=$(pwd)/${MBEDTLS_VERSION}
