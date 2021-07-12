#!/bin/bash
set -e
set -x

export OPENSSL_VERSION="1.1.1c"

# Set directory
export SCRIPTPATH=`pwd`

# Determine NDK path
export PROPS=$SCRIPTPATH/../../../../local.properties
OPENSSL_DIR=$SCRIPTPATH/openssl-$OPENSSL_VERSION

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`
export HOST_TAG=darwin-x86_64
export MIN_SDK_VERSION=21

export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$HOST_TAG

# openssl refers to the host specific toolchain as "ANDROID_NDK_HOME"
export ANDROID_NDK_HOME=$TOOLCHAIN
PATH=$TOOLCHAIN/bin:$PATH

mkdir -p build/openssl

# openssl does not handle api suffix well
ln -sfn $TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang $TOOLCHAIN/bin/$TARGET_HOST-clang

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

build_target_new() {
    export TARGET_HOST=$1
    export ANDROID_ARCH=$2

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
    ./Configure ${architecture} -D__ANDROID_API__=$ANDROID_API shared

    # path/to/openssl/Configure android-$ARCH -D__ANDROID_API__=$MINSDKVERSION --prefix=$INSTALLPATH --openssldir=$INSTALLPATH shared
    # make SHLIB_EXT=.so install_sw

    # Build
    make clean
    make SHLIB_EXT=.so

    # Copy the outputs
    OUTPUT_INCLUDE=$2/include
    echo $OUTPUT_INCLUDE
    OUTPUT_LIB=$2/lib/
    echo $OUTPUT_LIB
    mkdir -p $OUTPUT_INCLUDE
    mkdir -p $OUTPUT_LIB
    cp -RL include/openssl $OUTPUT_INCLUDE
    cp libcrypto.so $OUTPUT_LIB
    cp libcrypto.a $OUTPUT_LIB
    cp libssl.so $OUTPUT_LIB
    cp libssl.a $OUTPUT_LIB
   
    cd ../
}

get_openssl
build_target_new arm-linux-androideabi armeabi-v7a android-arm
build_target_new aarch64-linux-android arm64-v8a android-arm64
build_target_new i686-linux-android x86 android-x86
build_target_new x86_64-linux-android x86_64 android-x86_64
cd ../

#build_target "android-arm" "../../jni/openssl/openssl/armeabi-v7a"
#build_target "android-arm64" "../../jni/openssl/openssl/arm64-v8a"
#build_target "android-x86" "../../jni/openssl/openssl/x86"
#build_target "android-x86_64" "../../jni/openssl/openssl/x86_64"

echo OpenSSL build complete, exiting...
exit