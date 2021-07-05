#!/bin/bash
set -e
set -x

# Set directory
SCRIPTPATH=`pwd`
#export ANDROID_NDK_HOME=$SCRIPTPATH/android-ndk-r20
OPENSSL_DIR=$SCRIPTPATH/openssl-1.1.1c

build_target() {
    # Find the toolchain for your build machine
    toolchains_path=$(python toolchains_path.py --ndk ${ANDROID_NDK_HOME})

    # Configure the OpenSSL environment, refer to NOTES.ANDROID in OPENSSL_DIR
    # Set compiler clang, instead of gcc by default
    CC=clang

    # Add toolchains bin directory to PATH
    PATH=$toolchains_path/bin:$PATH

    # Set the Android API levels
    ANDROID_API=21

    # Set the target architecture
    # Can be android-arm, android-arm64, android-x86, android-x86 etc
    architecture=$1

    # Create the make file
    cd ${OPENSSL_DIR}
    ./Configure ${architecture} -D__ANDROID_API__=$ANDROID_API

    # Build
    make clean
    make

    # Copy the outputs
    OUTPUT_INCLUDE=$SCRIPTPATH/output/include
    OUTPUT_LIB=$SCRIPTPATH/output/lib/${architecture}
    mkdir -p $OUTPUT_INCLUDE
    mkdir -p $OUTPUT_LIB
    cp -RL include/openssl $OUTPUT_INCLUDE
    cp libcrypto.so $OUTPUT_LIB
    cp libcrypto.a $OUTPUT_LIB
    cp libssl.so $OUTPUT_LIB
    cp libssl.a $OUTPUT_LIB
   
    cd ../
}

build_target "android-arm"
build_target "android-arm64"
build_target "android-x86"
build_target "android-x86_64"

echo OpenSSL build complete, exiting...
exit