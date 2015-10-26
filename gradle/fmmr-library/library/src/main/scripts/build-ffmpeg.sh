#!/bin/sh

WORKING_DIR=`pwd`
export WORKING_DIR=$WORKING_DIR/src/main
export PROPS=$WORKING_DIR/../../../local.properties
SCRIPTS_DIR=$WORKING_DIR/scripts
FFMPEG_BUILD_DIR=$WORKING_DIR/ffmpeg
TARGET_ARMEABI_DIR=$WORKING_DIR/jni/ffmpeg/ffmpeg/armeabi
TARGET_ARMEABIV7A_DIR=$WORKING_DIR/jni/ffmpeg/ffmpeg/armeabi-v7a
TARGET_X86_DIR=$WORKING_DIR/jni/ffmpeg/ffmpeg/x86
TARGET_MIPS_DIR=$WORKING_DIR/jni/ffmpeg/ffmpeg/mips
SO_DIR=$FFMPEG_BUILD_DIR/build/ffmpeg

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`

if [ "$NDK" = "" ] || [ ! -d $NDK ]; then
	echo "NDK variable not set or path to NDK is invalid, exiting..."
	exit 1
fi

if [ "$#" -eq 1 ] && [ "$1" = "--enable-openssl" ]; then
    export SSL="$WORKING_DIR/jni/openssl-android"
    export SSL_LD="$WORKING_DIR"
    rm -rf $WORKING_DIR/jni/ffmpeg/ffmpeg/*
fi

if [ ! -d $FFMPEG_BUILD_DIR ]; then
    # Unpackage the FFmpeg archive
    tar -C  $WORKING_DIR -xvf $WORKING_DIR/ffmpeg-2.5.3.tar.gz
    # rm ffmpeg-0.11.1-android-2012-09-18.tar.gz
    mv $WORKING_DIR/ffmpeg-2.5.3 $FFMPEG_BUILD_DIR

    # Prepare the FFmpeg archive for building
    cd $FFMPEG_BUILD_DIR
   ./extract.sh

    cd $WORKING_DIR
fi

# Make the target JNI folder if it doesn't exist
if [ ! -d $WORKING_DIR/jni/ffmpeg/ffmpeg ] && ! mkdir -p $WORKING_DIR/jni/ffmpeg/ffmpeg; then
    echo "Error, could not make $WORKING_DIR/jni/ffmpeg/ffmpeg, exiting..."
    exit 1
fi

if [ ! -d $TARGET_ARMEABI_DIR ] || [ ! -d $TARGET_ARMEABIV7A_DIR ]; then
    # Build FFmpeg from ARM architecture and copy to the JNI folder
    cd $FFMPEG_BUILD_DIR
    ./arm-build.sh
    cd $WORKING_DIR
    echo "Copying FFmpeg .so files from $SO_DIR to $TARGET_ARMEABI_DIR"
    cp -r $SO_DIR/armeabi $TARGET_ARMEABI_DIR
    echo "Copying FFmpeg .so files from $SO_DIR to $TARGET_ARMEABIV7A_DIR"
    cp -r $SO_DIR/armeabi-v7a $TARGET_ARMEABIV7A_DIR
fi

if [ ! -d $TARGET_X86_DIR ]; then
    # Build FFmpeg from x86 architecture and copy to the JNI folder
    cd $FFMPEG_BUILD_DIR
    ./x86-build.sh
    cd $WORKING_DIR
    echo "Copying FFmpeg .so files from $SO_DIR to $TARGET_X86_DIR"
    cp -r $SO_DIR/x86 $TARGET_X86_DIR
fi

if [ ! -d $TARGET_MIPS_DIR ]; then
    # Build FFmpeg from MIPS architecture and copy to the JNI folder
    cd $FFMPEG_BUILD_DIR
    ./mips-build.sh
    cd $WORKING_DIR
    echo "Copying FFmpeg .so files from $SO_DIR to $TARGET_MIPS_DIR"
    cp -r $SO_DIR/mips $TARGET_MIPS_DIR
fi

echo Native build complete, exiting...
exit
