#!/bin/bash
set -e
set -x

cd ../
export SSL_LD=`pwd`/jni/openssl/openssl
cd ffmpeg

#./build-openssl.sh
./build-ffmpeg.sh

echo Native build complete, exiting...
exit