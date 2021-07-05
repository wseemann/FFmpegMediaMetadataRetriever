#!/bin/sh

cd src/main/ffmpeg

export WORKING_DIR=`pwd`
export PROPS=$WORKING_DIR/../../../../local.properties

TARGET_ARMEABIV7A_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/armeabi-v7a
TARGET_X86_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/x86
TARGET_X86_64_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/x86_64
TARGET_ARMEABI_64_DIR=$WORKING_DIR/../jni/ffmpeg/ffmpeg/arm64-v8a

export ENABLE_OPENSSL=false

export NDK=`grep ndk.dir $PROPS | cut -d'=' -f2`
export ANDROID_NDK_HOME=$NDK

build_target() {
    if [ "$ENABLE_OPENSSL" = true ] ; then
        echo 'Build FFmpeg with openssl support'
        ./build_openssl.sh $1
        ./build_ffmpeg_with_ssl.sh $1
    else
        ./ffmpeg-android-maker/ffmpeg-android-maker.sh $1
        export OUTPUT_DIR=${WORKING_DIR}/ffmpeg-android-maker/output/$1
        cp -r $OUTPUT_DIR $WORKING_DIR/../jni/ffmpeg/ffmpeg/
    fi
}

if [ "$NDK" = "" ] || [ ! -d $NDK ]; then
	echo "NDK variable not set or path to NDK is invalid, exiting..."
	exit 1
fi

if [ "$#" -eq 1 ] && [ "$1" = "--with-openssl" ]; then
    ENABLE_OPENSSL=true
    #rm -rf $WORKING_DIR/../jni/ffmpeg/ffmpeg/*
fi

# Make the target JNI folder if it doesn't exist
if [ ! -d $WORKING_DIR/../jni/ffmpeg/ffmpeg ] && ! mkdir -p $WORKING_DIR/../jni/ffmpeg/ffmpeg; then
    echo "Error, could not make $WORKING_DIR/jni/ffmpeg/ffmpeg, exiting..."
    exit 1
fi

if [ ! -d $TARGET_ARMEABIV7A_DIR ]; then
    # Build FFmpeg from ARM v7 architecture and copy to the JNI folder
    cd $WORKING_DIR
    build_target "armeabi-v7a"
fi

if [ ! -d $TARGET_X86_DIR ]; then
    # Build FFmpeg from x86 architecture and copy to the JNI folder
    cd $WORKING_DIR
    build_target "x86"
fi

if [ ! -d $TARGET_X86_64_DIR ]; then
    # Build FFmpeg from x86_64 architecture and copy to the JNI folder
    cd $WORKING_DIR
    build_target x86_64
fi

if [ ! -d $TARGET_ARMEABI_64_DIR ]; then
    # Build FFmpeg from arneabi_64 architecture and copy to the JNI folder
    cd $WORKING_DIR
    build_target arm64-v8a
fi

echo Native build complete, exiting...
exit
