#!/bin/bash
set -e
set -x

# cd to the build script directory
cd ../
export SSL_LD=`pwd`/jni/openssl/openssl
cd ffmpeg

# Set the build variables needed by ffmpeg-android-maker.sh
export WORKING_DIR=`pwd`
export PROPS=$WORKING_DIR/../../../../local.properties
export ANDROID_NDK_HOME=`grep ndk.dir $PROPS | cut -d'=' -f2`
export ANDROID_SDK_HOME=`grep sdk.dir $PROPS | cut -d'=' -f2`

./build-openssl.sh
./build-ffmpeg.sh

#echo Native build complete, exiting...
exit
