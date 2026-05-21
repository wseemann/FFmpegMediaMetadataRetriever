#!/bin/bash
set -e

ABIS=("all" "armv7" "x86" "x86_64" "arm64")

if [[ "$1" == "--local" ]]; then
    TASK="publishReleasePublicationToLocalRepository"
elif [[ "$1" == "--maven-local" ]]; then
    TASK="publishReleasePublicationToMavenLocal"
else
    TASK="publishReleasePublicationToOssrh-staging-apiRepository"
fi

echo "Publishing :core..."
./gradlew ":core:$TASK"

for abi in "${ABIS[@]}"; do
    echo "Publishing :native (abi=$abi)..."
    ./gradlew ":native:$TASK" -Pabi="$abi"
done

echo "Done."