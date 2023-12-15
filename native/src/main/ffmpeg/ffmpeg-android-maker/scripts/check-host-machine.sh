#!/usr/bin/env bash

function checkVariablePresence() {
  VARIABLE_NAME=$1
  if [[ -z "${!VARIABLE_NAME}" ]]; then
    echo "The ${VARIABLE_NAME} environment variable isn't defined"
    echo $2
    exit 1
  fi
}

checkVariablePresence "ANDROID_SDK_HOME" \
  "The variable should be set to the actual Android SDK path" || exit 1

checkVariablePresence "ANDROID_NDK_HOME" \
  "The variable should be set to the actual Android NDK path" || exit 1
