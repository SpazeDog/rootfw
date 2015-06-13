#!/bin/bash

BUILD_PATH="$(readlink -f "$(dirname $0)")"
BUILD_TYPE="$1"

export PATH="$PATH:$BUILD_PATH"

if [ -z "$BUILD_TYPE" ] || [[ "$BUILD_TYPE" != "debug" && "$BUILD_TYPE" != "release" ]]; then
    BUILD_TYPE="release"
fi

if [ -f "$BUILD_PATH/gradlew" ]; then
    chmod +x "$BUILD_PATH/gradlew" 2> /dev/null
fi

if which gradlew 2>&1 > /dev/null; then
    # Build RootFW
    cd "$BUILD_PATH/projects/rootfw" || exit 1
    gradlew clean || exit 1
    gradlew build || exit 1
    cp -f "$BUILD_PATH/build/rootfw/outputs/aar/rootfw-$BUILD_TYPE.aar" "$BUILD_PATH/projects" || exit 1

    # Clean
    cd "$BUILD_PATH" || exit 1
    gradlew clean || exit 1

else
    echo "You need to setup Gradle to build this project"
fi
