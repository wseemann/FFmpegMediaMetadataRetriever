#!/usr/bin/env bash

# Defining essential directories

# The root of the project
export BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"
# Directory that contains source code for FFmpeg and its dependencies
# Each library has its own subdirectory
# Multiple versions of the same library can be stored inside librarie's directory
export SOURCES_DIR=${BASE_DIR}/sources
# Directory to place some statistics about the build.
# Currently - the info about Text Relocations
export STATS_DIR=${BASE_DIR}/stats
# Directory that contains helper scripts and
# scripts to download and build FFmpeg and each dependency separated by subdirectories
export SCRIPTS_DIR=${BASE_DIR}/scripts
# The directory to use by Android project
# All FFmpeg's libraries and headers are copied there
export OUTPUT_DIR=${BASE_DIR}/output

# Directory to use as a place to build/install FFmpeg and its dependencies
BUILD_DIR=${BASE_DIR}/build
# Separate directory to build FFmpeg to
export BUILD_DIR_FFMPEG=$BUILD_DIR/ffmpeg
# All external libraries are installed to a single root
# to make easier referencing them when FFmpeg is being built.
export BUILD_DIR_EXTERNAL=$BUILD_DIR/external

# Function that copies *.so files and headers of the current ANDROID_ABI
# to the proper place inside OUTPUT_DIR
function prepareOutput() {
  OUTPUT_LIB=${OUTPUT_DIR}/${ANDROID_ABI}/lib/
  mkdir -p ${OUTPUT_LIB}
  cp ${BUILD_DIR_FFMPEG}/${ANDROID_ABI}/lib/*.so ${OUTPUT_LIB}

  OUTPUT_HEADERS=${OUTPUT_DIR}/${ANDROID_ABI}/include/
  mkdir -p ${OUTPUT_HEADERS}
  cp -r ${BUILD_DIR_FFMPEG}/${ANDROID_ABI}/include/* ${OUTPUT_HEADERS}
}

# Saving stats about text relocation presence.
# If the result file doesn't have 'TEXTREL' at all, then we are good.
# Otherwise the whole script is interrupted
function checkTextRelocations() {
  TEXT_REL_STATS_FILE=${STATS_DIR}/text-relocations.txt
  ${FAM_READELF} --dynamic ${BUILD_DIR_FFMPEG}/${ANDROID_ABI}/lib/*.so | grep 'TEXTREL\|File' >> ${TEXT_REL_STATS_FILE}

  if grep -q TEXTREL ${TEXT_REL_STATS_FILE}; then
    echo "There are text relocations in output files:"
    cat ${TEXT_REL_STATS_FILE}
    exit 1
  fi
}

# Actual work of the script

# Clearing previously created binaries
rm -rf ${BUILD_DIR}
rm -rf ${STATS_DIR}
rm -rf ${OUTPUT_DIR}
mkdir -p ${STATS_DIR}
mkdir -p ${OUTPUT_DIR}

# Exporting more necessary variabls
source ${SCRIPTS_DIR}/export-host-variables.sh
source ${SCRIPTS_DIR}/parse-arguments.sh

# Treating FFmpeg as just a module to build after its dependencies
COMPONENTS_TO_BUILD=${EXTERNAL_LIBRARIES[@]}
COMPONENTS_TO_BUILD+=( "ffmpeg" )

# Get the source code of component to build
for COMPONENT in ${COMPONENTS_TO_BUILD[@]}
do
  echo "Getting source code of the component: ${COMPONENT}"
  SOURCE_DIR_FOR_COMPONENT=${SOURCES_DIR}/${COMPONENT}

  mkdir -p ${SOURCE_DIR_FOR_COMPONENT}
  cd ${SOURCE_DIR_FOR_COMPONENT}

  # Executing the component-specific script for downloading the source code
  source ${SCRIPTS_DIR}/${COMPONENT}/download.sh

  # The download.sh script has to export SOURCES_DIR_$COMPONENT variable
  # with actual path of the source code. This is done for possiblity to switch
  # between different verions of a component.
  # If it isn't set, consider SOURCE_DIR_FOR_COMPONENT as the proper value
  COMPONENT_SOURCES_DIR_VARIABLE=SOURCES_DIR_${COMPONENT}
  if [[ -z "${!COMPONENT_SOURCES_DIR_VARIABLE}" ]]; then
     export SOURCES_DIR_${COMPONENT}=${SOURCE_DIR_FOR_COMPONENT}
  fi

  # Returning to the rood directory. Just in case.
  cd ${BASE_DIR}
done

# ABIs to build FFmpeg for.
# x86 is the first, because it is likely to have Text Relocations.
# In this case the rest ABIs will not be assembled at all.
#ABIS_TO_BUILD=( "x86" "x86_64" "armeabi-v7a" "arm64-v8a" )
ABIS_TO_BUILD=( $1 )
echo $ABIS_TO_BUILD

for ABI in ${ABIS_TO_BUILD[@]}
do
  # Exporting variables for the current ABI
  source ${SCRIPTS_DIR}/export-build-variables.sh ${ABI}

  for COMPONENT in ${COMPONENTS_TO_BUILD[@]}
  do
    echo "Building the component: ${COMPONENT}"
    COMPONENT_SOURCES_DIR_VARIABLE=SOURCES_DIR_${COMPONENT}

    # Going to the actual source code directory of the current component
    cd ${!COMPONENT_SOURCES_DIR_VARIABLE}

    # and executing the component-specific build script
    ${SCRIPTS_DIR}/${COMPONENT}/build.sh

    # Returning to the root directory. Just in case.
    cd ${BASE_DIR}
  done

  checkTextRelocations

  prepareOutput
done
