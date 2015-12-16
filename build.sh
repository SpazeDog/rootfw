#!/bin/bash

BUILD_PATH="$(readlink -f "$(dirname $0)")"
BUILD_TYPE="$1"
BUILD_ACTION="$2"
BUILD_HOME=~/.gradle/build/$(basename "$BUILD_PATH")
BUILD_LIB="$BUILD_HOME/rootfw"
BUILD_BINTRAY="$BUILD_HOME/bintray"

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
    cp -f "$BUILD_LIB/outputs/aar/rootfw-$BUILD_TYPE.aar" "$BUILD_PATH/projects/" || exit 1

    # BitTray Upload
    if [ "$BUILD_ACTION" = "publish" ]; then
        if [ ! -f "$BUILD_PATH/bintray.properties" ]; then
            echo "Cannot publish. Missing Bintray Properties!"; exit 1

        elif ! which curl > /dev/null 2>&1; then
            echo "Cannot publish. Missing 'curl' binary"; exit 1
        fi

        rm -rf "$BUILD_BINTRAY" 2> /dev/null
        mkdir -p "$BUILD_BINTRAY/META_INF"

cat << 'EOF' > "$BUILD_BINTRAY/META_INF/MANIFEST.MF"
Manifest-Version: 1.0
EOF

cat << 'EOF' > "$BUILD_BINTRAY/library.pom"
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>%groupid</groupId>
  <artifactId>%artifactid</artifactId>
  <version>%version</version>
</project>
EOF

        cd "$BUILD_BINTRAY"
        zip -r "javadoc.jar" "META_INF/" || exit 1

        cd "$BUILD_PATH/src/"
        zip -r "$BUILD_BINTRAY/sources.jar" * || exit 1
        cd "$BUILD_PATH"

        cp "$BUILD_PATH/projects/rootfw-$BUILD_TYPE.aar" "$BUILD_BINTRAY/library.aar" || exit 1

        btPackage="rootfw_gen4"
        btGroupId="com.spazedog.lib"
        btVersion="$(grep 'versionName' "$BUILD_PATH/projects/rootfw/build.gradle" | sed 's/.*\"\(.*\)\"/\1/')"
        btBaseDir="$(echo $btGroupId | tr . /)/$btPackage/$btVersion"

        if [ "$BUILD_TYPE" = "debug" ]; then
            btVersion="$btVersion-debug"
        fi

        sed -i "s/%groupid/$btGroupId/" "$BUILD_BINTRAY/library.pom"
        sed -i "s/%artifactid/$btPackage/" "$BUILD_BINTRAY/library.pom"
        sed -i "s/%version/$btVersion/" "$BUILD_BINTRAY/library.pom"

        curl -u $(cat "$BUILD_PATH/bintray.properties") -T "$BUILD_BINTRAY/sources.jar" "https://api.bintray.com/content/dk-zero-cool/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion-sources.jar?publish=1" 
        curl -u $(cat "$BUILD_PATH/bintray.properties") -T "$BUILD_BINTRAY/javadoc.jar" "https://api.bintray.com/content/dk-zero-cool/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion-javadoc.jar?publish=1" 
        curl -u $(cat "$BUILD_PATH/bintray.properties") -T "$BUILD_BINTRAY/library.aar" "https://api.bintray.com/content/dk-zero-cool/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion.aar?publish=1" 
        curl -u $(cat "$BUILD_PATH/bintray.properties") -T "$BUILD_BINTRAY/library.pom" "https://api.bintray.com/content/dk-zero-cool/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion.pom?publish=1" 

        rm -rf "$BUILD_BINTRAY"
    fi

    # Clean
    cd "$BUILD_PATH" || exit 1
    gradlew clean || exit 1

else
    echo "You need to setup Gradle to build this project"
fi
