#!/bin/bash

#############################################
#
# ASSEMBLE VARIABLES
#

BUILD_TYPE=$1

if [ -z "$BUILD_TYPE" ] || [[ "$BUILD_TYPE" != "debug" && "$BUILD_TYPE" != "release" ]]; then
    BUILD_TYPE="release"
fi

BUILD_SRC="$(dirname "$(readlink -f "$0")")"
BUILD_NAME=$(basename "$BUILD_SRC")

LIB_DST=~/.gradle/build/$BUILD_NAME/rootfw
LIB_SRC="$BUILD_SRC/src"
LIB_PROJECT="$BUILD_SRC/projects/rootfw"
LIB_FILE=$LIB_DST/outputs/aar/rootfw-$BUILD_TYPE.aar
LIB_PKG="$LIB_PROJECT/../rootfw-$BUILD_TYPE.aar"

BINTRAY_DST=~/.gradle/build/$BUILD_NAME/bintray
BINTRAY_PACKAGE="rootfw_gen4"
BINTRAY_GROUP="com.spazedog.lib"


#############################################
#
# PREPARE GRADLEW
#

if [ ! -f "$BUILD_SRC/gradlew" ]; then
    echo "Missing 'gradlew' in src directory"; exit 1

else
    chmod +x "$BUILD_SRC/gradlew" || exit 1
    PATH="$BUILD_SRC:$PATH"
fi


#############################################
#
# BUILD PROJECT
#

cd "$LIB_PROJECT"

gradlew clean || exit 1

if [ "$BUILD_TYPE" = "release" ]; then
    gradlew assembleRelease || exit 1

else
    gradlew --debug assembleDebug || exit 1
fi

if [ -f "$LIB_FILE" ]; then
    mv -f "$LIB_FILE" "$LIB_PKG" || exit 1

else
    echo "Output aar file does not exist"; exit 1
fi


#############################################
#
# SIGN PACKAGE
#

echo -n "Do you want to sign the library? [Y/N]: "

stty_bak=$(stty -g)
stty raw -echo
answer=$( while ! head -c 1 | grep -i '[ny]'; do true; done )
stty $stty_bak

echo $answer

if echo $answer | grep -iq "^y"; then
    if which jarsigner > /dev/null 2>&1; then
        read -e -p "KeyStore: " KEYSTORE

        if [ -f "$KEYSTORE" ]; then
            KEYSTORE="$(readlink -f "$KEYSTORE")"

            read -p "Alias: " ALIAS
            read -s -p "Store Password: " STOREPWD
            echo ""
            read -s -p "Key Password: [Optional] " KEYPWD
            echo ""

            if [ -z "$KEYPWD" ]; then
                KEYPWD=$STOREPWD
            fi

            zip -d "$LIB_PKG" META-INF/* 2> /dev/null
            jarsigner -tsa http://timestamp.digicert.com -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore "$KEYSTORE" -storepass $STOREPWD -keypass $KEYPWD "$LIB_PKG" $ALIAS || exit 1

        else
            echo "Could not find the KeyStore at the specified location"; exit 1
        fi

    else
        echo "Could not locate the binary 'jarsigner'"; exit 1
    fi
fi


#############################################
#
# PUBLISH PACKAGE
#

if [ -n "$KEYSTORE" ]; then
    echo -n "Do you want to publish the library? [Y/N]: "

    stty_bak=$(stty -g)
    stty raw -echo
    answer=$( while ! head -c 1 | grep -i '[ny]'; do true; done )
    stty $stty_bak

    echo $answer

    if echo $answer | grep -iq "^y"; then
        if which curl > /dev/null 2>&1; then
            read -e -p "Bintray KeyFile: " KEYFILE

            if [ -f "$KEYFILE" ]; then
                KEYFILE="$(readlink -f "$KEYFILE")"

                read -p "Bintray Username: " USERNAME

                rm -rf "$BINTRAY_DST" 2> /dev/null
                mkdir -p "$BINTRAY_DST/META_INF"

cat << 'EOF' > "$BINTRAY_DST/META_INF/MANIFEST.MF"
Manifest-Version: 1.0
EOF

cat << 'EOF' > "$BINTRAY_DST/library.pom"
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>%groupid</groupId>
  <artifactId>%artifactid</artifactId>
  <version>%version</version>
</project>
EOF

                cd "$BINTRAY_DST"
                zip -r "javadoc.jar" "META_INF/" || exit 1

                cd "$DEP_SRC"
                zip -r "$BINTRAY_DST/sources.jar" * || exit 1
                cd "$LIB_SRC"
                zip -r "$BINTRAY_DST/sources.jar" * || exit 1
                cd "$BINTRAY_DST"

                cp "$LIB_PKG" "$BINTRAY_DST/library.aar" || exit 1

                if ! which aapt > /dev/null 2>&1; then
                    if [ -f "$BUILD_SRC/local.properties" ] && grep -qe '^sdk.dir' "$BUILD_SRC/local.properties"; then
                        BUILD_SDK=$(grep -e '^sdk.dir' "$BUILD_SRC/local.properties" | sed 's/sdk\.dir=\(.*\)/\1/')

                        if [ -d "$BUILD_SDK" ]; then
                            AAPT=$(find "$BUILD_SDK" -name 'aapt' | head -n 1)

                            if [ -f "$AAPT" ]; then
                                PATH="$(dirname $AAPT):$PATH"
                            fi
                        fi
                    fi
                fi

                if which aapt > /dev/null 2>&1 && aapt dump badging "$BINTRAY_DST/library.aar" > /dev/null 2>&1 | grep -q "package:"; then
                    btVersion="$(aapt dump badging "$BINTRAY_DST/library.aar" | grep 'package:' | sed 's/.*versionName=\x27\([^\x27]\+\)\x27.*/\1/')"

                elif grep -q 'versionName' "$LIB_PROJECT/build.gradle"; then
                    btVersion="$(grep 'versionName' "$LIB_PROJECT/build.gradle" | sed 's/.*\"\(.*\)\"/\1/')"

                else
                    echo "Could not get version information from the package"; exit 1
                fi

                btPackage="$BINTRAY_PACKAGE"
                btGroupId="$BINTRAY_GROUP"
                btBaseDir="$(echo $btGroupId | tr . /)/$btPackage/$btVersion"

                if [ "$BUILD_TYPE" = "debug" ]; then
                    btVersion="$btVersion-debug"
                fi

                sed -i "s/%groupid/$btGroupId/" "$BINTRAY_DST/library.pom" || exit 1
                sed -i "s/%artifactid/$btPackage/" "$BINTRAY_DST/library.pom" || exit 1
                sed -i "s/%version/$btVersion/" "$BINTRAY_DST/library.pom" || exit 1

                jarsigner -tsa http://timestamp.digicert.com -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore "$KEYSTORE" -storepass $STOREPWD -keypass $KEYPWD "$BINTRAY_DST/javadoc.jar" $ALIAS || exit 1
                jarsigner -tsa http://timestamp.digicert.com -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore "$KEYSTORE" -storepass $STOREPWD -keypass $KEYPWD "$BINTRAY_DST/sources.jar" $ALIAS || exit 1

cat << EOF
The following files have been assembled:

    $btPackage-$btVersion-sources.jar
    $btPackage-$btVersion-javadoc.jar
    $btPackage-$btVersion.aar
    $btPackage-$btVersion.pom
EOF

                echo ""
                echo -n "Do you wish to continue publishing these files? [Y/N]: "

                stty_bak=$(stty -g)
                stty raw -echo
                answer=$( while ! head -c 1 | grep -i '[ny]'; do true; done )
                stty $stty_bak

                echo $answer

                if echo $answer | grep -iq "^y"; then
                    curl -u $(cat "$KEYFILE") -T "$BINTRAY_DST/sources.jar" "https://api.bintray.com/content/$USERNAME/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion-sources.jar?publish=1" || exit 1
                    curl -u $(cat "$KEYFILE") -T "$BINTRAY_DST/javadoc.jar" "https://api.bintray.com/content/$USERNAME/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion-javadoc.jar?publish=1" || exit 1
                    curl -u $(cat "$KEYFILE") -T "$BINTRAY_DST/library.aar" "https://api.bintray.com/content/$USERNAME/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion.aar?publish=1" || exit 1
                    curl -u $(cat "$KEYFILE") -T "$BINTRAY_DST/library.pom" "https://api.bintray.com/content/$USERNAME/maven/$btPackage/$btVersion/$btBaseDir/$btPackage-$btVersion.pom?publish=1" || exit 1
                fi

            else
                echo "Could not find the Bintray KeyFile at the specified location"; exit 1
            fi

        else
            echo "You need to install curl in order to publish the library"; exit 1
        fi
    fi
fi

