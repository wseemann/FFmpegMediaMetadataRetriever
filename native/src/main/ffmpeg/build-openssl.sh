#!/bin/bash
set -e
set -x

export OPENSSL_VERSION="1.1.1w"

# Set directory
export SCRIPTPATH=`pwd`

# Determine NDK path
export PROPS=$SCRIPTPATH/../../../../local.properties
OPENSSL_DIR=$SCRIPTPATH/openssl-$OPENSSL_VERSION

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`
export HOST_TAG=darwin-x86_64
export MIN_SDK_VERSION=16

export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$HOST_TAG

# openssl refers to the host specific toolchain as "ANDROID_NDK_HOME"
export ANDROID_NDK_HOME=$NDK
PATH=$TOOLCHAIN/bin:$PATH

mkdir -p build/openssl

get_openssl() {
    if [ ! -e "openssl-${OPENSSL_VERSION}.tar.gz" ]; then
        echo "Downloading openssl-${OPENSSL_VERSION}.tar.gz"
        curl -LO https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz
    else
        echo "Using openssl-${OPENSSL_VERSION}.tar.gz"
    fi

    tar -xvf openssl-${OPENSSL_VERSION}.tar.gz
    cd $OPENSSL_DIR
}

build_target() {
    export TARGET_HOST=$1
    export ANDROID_ARCH=$2
    # Enables 16kb page sizes on Android
    # See: https://developer.android.com/guide/practices/page-sizes
    LDFLAGS="-Wl,-z,max-page-size=16384"

    ./Configure $3 shared \
    -D__ANDROID_API__=$MIN_SDK_VERSION \
     --prefix=$PWD/build/$ANDROID_ARCH

    make clean
    #  make SHLIB_VERSION_NUMBER= SHLIB_EXT=.so install_sw
    make install_sw
    mkdir -p ../build/openssl/$ANDROID_ARCH
    cp -R $PWD/build/$ANDROID_ARCH ../build/openssl/
    #rm -rf ../build/openssl/$ANDROID_ARCH/lib
    #mkdir ../build/openssl/$ANDROID_ARCH/lib
    cp libcrypto.so ../build/openssl/$ANDROID_ARCH/lib/
    cp libcrypto.a ../build/openssl/$ANDROID_ARCH/lib/
    cp libssl.so ../build/openssl/$ANDROID_ARCH/lib/
    cp libssl.a ../build/openssl/$ANDROID_ARCH/lib/
}

remove_openssl() {
    if [ -e $OPENSSL_DIR ]; then
        echo Cleaning up build directory $OPENSSL_DIR
        rm -rf $OPENSSL_DIR
    fi
}


get_openssl
export MIN_SDK_VERSION=21
build_target arm-linux-androideabi armeabi-v7a android-arm
export MIN_SDK_VERSION=21
build_target aarch64-linux-android arm64-v8a android-arm64
export MIN_SDK_VERSION=21
build_target i686-linux-android x86 android-x86
export MIN_SDK_VERSION=21
build_target x86_64-linux-android x86_64 android-x86_64
remove_openssl
cd ../
rm -r ../jni/openssl/openssl/
cp -r build/ ../jni/openssl/

echo OpenSSL build complete, exiting...
exit