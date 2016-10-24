#!/bin/sh

cd src/main/ffmpeg

export WORKING_DIR=`pwd`
export PROPS=$WORKING_DIR/../../../../local.properties

TARGET_ARMEABI_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/armeabi
TARGET_ARMEABIV7A_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/armeabi-v7a
TARGET_X86_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/x86
TARGET_MIPS_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/mips
TARGET_X86_64_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/x86_64
TARGET_ARMEABI_64_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/arm64-v8a

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`

if [ "$NDK" = "" ] || [ ! -d $NDK ]; then
	echo "NDK variable not set or path to NDK is invalid, exiting..."
	exit 1
fi

#if [ "$#" -eq 1 ] && [ "$1" = "--enable-openssl" ]; then
#    export SSL="$WORKING_DIR/jni/openssl-android"
#    export SSL_LD="$WORKING_DIR"
#    rm -rf $WORKING_DIR/jni/ffmpeg/ffmpeg/*
#fi

# Make the target JNI folder if it doesn't exist
if [ ! -d $WORKING_DIR/../jni/ffmpeg/ffmpeg ] && ! mkdir -p $WORKING_DIR/../jni/ffmpeg/ffmpeg; then
    echo "Error, could not make $WORKING_DIR/jni/ffmpeg/ffmpeg, exiting..."
    exit 1
fi

if [ ! -d $TARGET_ARMEABI_DIR ]; then
    # Build FFmpeg from ARM architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh arm
fi

if [ ! -d $TARGET_ARMEABIV7A_DIR ]; then
    # Build FFmpeg from ARM v7 architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh armv7-a
fi

if [ ! -d $TARGET_X86_DIR ]; then
    # Build FFmpeg from x86 architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh i686
fi

if [ ! -d $TARGET_MIPS_DIR ]; then
    # Build FFmpeg from MIPS architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh mips
fi

if [ ! -d $TARGET_X86_64_DIR ]; then
    # Build FFmpeg from x86_64 architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh x86_64
fi

if [ ! -d $TARGET_ARMEABI_64_DIR ]; then
    # Build FFmpeg from arneabi_64 architecture and copy to the JNI folder
    cd $WORKING_DIR
    ./build_ffmpeg.sh arm64-v8a
fi

echo Native build complete, exiting...
exit
