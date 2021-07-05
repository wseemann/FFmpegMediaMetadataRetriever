#!/usr/bin/env bash

# This script parses arguments that were passed to ffmpeg-android-maker.sh
# and exports a bunch of varables that are used elsewhere.

# Local variables with default values. Can be overridden with specific arguments
# See the end of this file for more description
EXTERNAL_LIBRARIES=()
SOURCE_TYPE=TAR
SOURCE_VALUE=4.2.2
API_LEVEL=16

for artument in "$@"
do
  case $artument in
    # Use this value as Android platform version during compilation.
    --android-api-level=*)
      API_LEVEL="${artument#*=}"
      shift
    ;;
    # Checkout the particular tag in the FFmpeg's git repository
    --source-git-tag=*)
      SOURCE_TYPE=GIT_TAG
      SOURCE_VALUE="${artument#*=}"
      shift
    ;;
    # Checkout the particular branch in the FFmpeg's git repository
    --source-git-branch=*)
      SOURCE_TYPE=GIT_BRANCH
      SOURCE_VALUE="${artument#*=}"
      shift
    ;;
    # Download the particular tar archive by its version
    --source-tar=*)
      SOURCE_TYPE=TAR
      SOURCE_VALUE="${artument#*=}"
      shift
    ;;
    # Arguments below enable certain external libraries to build into FFmpeg
    --enable-libaom)
      EXTERNAL_LIBRARIES+=( "libaom" )
      shift
    ;;
    --enable-libdav1d)
      EXTERNAL_LIBRARIES+=( "libdav1d" )
      shift
    ;;
    --enable-libmp3lame)
      EXTERNAL_LIBRARIES+=( "libmp3lame" )
      shift
    ;;
    *)
      echo "Unknown argument $artument"
    ;;
  esac
done

# Saving the information FFmpeg's source code downloading
export FFMPEG_SOURCE_TYPE=$SOURCE_TYPE
export FFMPEG_SOURCE_VALUE=$SOURCE_VALUE

# A list of external libraries to build into the FFMpeg
# Elements from this list are used for strings substitution
export FFMPEG_EXTERNAL_LIBRARIES=${EXTERNAL_LIBRARIES[@]}

# Desired Android API level to use during compilation
# Will be replaced with 21 for 64bit ABIs if the value is less than 21
export DESIRED_ANDROID_API_LEVEL=${API_LEVEL}
