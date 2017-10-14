#!/bin/bash
# Cross-compile environment for Android on ARMv7 and x86
#

export WORKING_DIR=`pwd`
export PROPS=$WORKING_DIR/../../../../local.properties

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`

if [ "$NDK" = "" ] || [ ! -d $NDK ]; then
  echo "NDK variable not set or path to NDK is invalid, exiting..."
  exit 1
fi

export ANDROID_NDK_ROOT=$NDK

if [ $# -ne 1 ];
  then echo "illegal number of parameters"
  echo "usage: build_openssl.sh TARGET"
  exit 1
fi

export TARGET=$1

ARM_PLATFORM=$NDK/platforms/android-9/arch-arm/
ARM_PREBUILT=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64

ARM64_PLATFORM=$NDK/platforms/android-21/arch-arm64/
ARM64_PREBUILT=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/darwin-x86_64

X86_PLATFORM=$NDK/platforms/android-9/arch-x86/
X86_PREBUILT=$NDK/toolchains/x86-4.9/prebuilt/darwin-x86_64

X86_64_PLATFORM=$NDK/platforms/android-21/arch-x86_64/
X86_64_PREBUILT=$NDK/toolchains/x86_64-4.9/prebuilt/darwin-x86_64

MIPS_PLATFORM=$NDK/platforms/android-9/arch-mips/
MIPS_PREBUILT=$NDK/toolchains/mipsel-linux-android-4.9/prebuilt/darwin-x86_64

BUILD_DIR=`pwd`/openssl-android

OPENSSL_VERSION="1.0.2j"

if [ ! -e "openssl-${OPENSSL_VERSION}.tar.gz" ]; then
  echo "Downloading openssl-${OPENSSL_VERSION}.tar.gz"
  curl -LO https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz
else
  echo "Using openssl-${OPENSSL_VERSION}.tar.gz"
fi

if [ -d openssl-${OPENSSL_VERSION} ]
then
    rm -rf openssl-${OPENSSL_VERSION}
fi

tar -xvf openssl-${OPENSSL_VERSION}.tar.gz

function build_one
{
if [ $ARCH == "arm" ]
then
    PLATFORM=$ARM_PLATFORM
    PREBUILT=$ARM_PREBUILT
    HOST=arm-linux-androideabi
#added by alexvas
elif [ $ARCH == "arm64" ]
then
    PLATFORM=$ARM64_PLATFORM
    PREBUILT=$ARM64_PREBUILT
    HOST=aarch64-linux-android
elif [ $ARCH == "mips" ]
then
    PLATFORM=$MIPS_PLATFORM
    PREBUILT=$MIPS_PREBUILT
    HOST=mipsel-linux-android
#alexvas
elif [ $ARCH == "x86_64" ]
then
    PLATFORM=$X86_64_PLATFORM
    PREBUILT=$X86_64_PREBUILT
    HOST=x86_64-linux-android
else
    PLATFORM=$X86_PLATFORM
    PREBUILT=$X86_PREBUILT
    HOST=i686-linux-android
fi

INSTALL_DIR=`pwd`/openssl-android/$CPU
mkdir -p $INSTALL_DIR

. ./Setenv-android.sh $NDK $ANDROID_EABI $ANDROID_ARCH
cd openssl-${OPENSSL_VERSION}

if [ $TARGET == "mips" ]
then
    ./Configure android-mips shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir=$INSTALL_DIR --prefix=$INSTALL_DIR
elif [ $TARGET == "x86_64" ]
then
    #./Configure linux-generic64 shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir=$INSTALL_DIR --prefix=$INSTALL_DIR
    . ../libopenssl_builder.sh -s `pwd` -n $NDK -o $BUILD_DIR -b 6

    # copy the binaries
    mkdir -p $PREFIX
    cp -r $BUILD_DIR/$CPU/* $PREFIX

    exit 0
elif [ $TARGET == "arm64-v8a" ]
then
    . ../libopenssl_builder.sh -s `pwd` -n $NDK -o $BUILD_DIR -b 1

    # copy the binaries
    mkdir -p $PREFIX
    cp -r $BUILD_DIR/$CPU/* $PREFIX

    exit 0
else
    ./config shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir=$INSTALL_DIR --prefix=$INSTALL_DIR
fi

make depend
make all

echo $ANDROID_TOOLCHAIN
echo $PREBUILT/bin

echo 'Xxxxxxxxxx' | sudo -kSE make install CC=$PREBUILT/bin/$HOST-gcc RANLIB=$PREBUILT/bin/$HOST-ranlib

# copy the binaries
mkdir -p $PREFIX
cp -r $BUILD_DIR/$CPU/* $PREFIX
}

if [ $TARGET == 'arm' ]; then
  CPU=arm
  ARCH=arm
  PREFIX=`pwd`/../jni/openssl-android/armeabi
  export ANDROID_EABI=arm-linux-androideabi-4.9
  export ANDROID_ARCH=arch-arm
  build_one
fi

if [ $TARGET == 'armv7-a' ]; then
  CPU=armv7-a
  ARCH=arm
  PREFIX=`pwd`/../jni/openssl-android/armeabi-v7a
  export ANDROID_EABI=arm-linux-androideabi-4.9
  export ANDROID_ARCH=arch-arm
  build_one
fi

if [ $TARGET == 'i686' ]; then
  CPU=i686
  ARCH=i686
  PREFIX=`pwd`/../jni/openssl-android/x86
  export ANDROID_EABI=x86-4.9
  export ANDROID_ARCH=arch-x86
  build_one
fi

if [ $TARGET == 'mips' ]; then
  CPU=mips
  ARCH=mips
  PREFIX=`pwd`/../jni/openssl-android/mips
  export ANDROID_EABI=mipsel-linux-android-4.9
  export ANDROID_ARCH=arch-mips
  build_one
fi

if [ $TARGET == 'x86_64' ]; then
  CPU=x86_64
  ARCH=x86_64
  PREFIX=`pwd`/../jni/openssl-android/x86_64
  export ANDROID_EABI=x86_64-4.9
  export ANDROID_ARCH=arch-x86_64
  build_one
fi

if [ $TARGET == 'arm64-v8a' ]; then
  CPU=arm64-v8a
  ARCH=arm64
  PREFIX=`pwd`/../jni/openssl-android/arm64-v8a
  export ANDROID_EABI=aarch64-linux-android-4.9
  export ANDROID_ARCH=arch-arm64
  build_one
fi
