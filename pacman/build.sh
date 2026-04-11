#!/bin/bash
set -e

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
AAPT="/usr/bin/aapt"
DX="/usr/lib/android-sdk/build-tools/debian/dx"
APKSIGNER="/usr/bin/apksigner"
ZIPALIGN="/usr/bin/zipalign"
KEYSTORE="debug.keystore"

cd "$(dirname "$0")"

echo "=== Cleaning build dirs ==="
rm -rf gen obj bin
mkdir -p gen obj bin

echo "=== Generating R.java ==="
$AAPT package -f -m \
    -J gen \
    -M AndroidManifest.xml \
    -S res \
    -I "$ANDROID_JAR"

echo "=== Compiling Java sources ==="
SOURCES=$(find src gen -name "*.java" 2>/dev/null)
javac \
    --release 8 \
    -classpath "$ANDROID_JAR" \
    -d obj \
    $SOURCES

echo "=== Converting to DEX ==="
$DX --dex --output=bin/classes.dex obj

echo "=== Packaging resources into APK ==="
$AAPT package -f \
    -M AndroidManifest.xml \
    -S res \
    -I "$ANDROID_JAR" \
    -F bin/pacman-unsigned.apk

echo "=== Adding classes.dex to APK ==="
cd bin
$AAPT add pacman-unsigned.apk classes.dex
cd ..

echo "=== Zipalign ==="
$ZIPALIGN -f 4 bin/pacman-unsigned.apk bin/pacman-aligned.apk

echo "=== Generating debug keystore ==="
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkeypair \
        -keystore "$KEYSTORE" \
        -alias debug \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass android \
        -keypass android \
        -dname "CN=Debug,O=PacMan,C=US" \
        -noprompt
fi

echo "=== Signing APK ==="
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out bin/pacman.apk \
    bin/pacman-aligned.apk

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK: $(pwd)/bin/pacman.apk"
ls -lh bin/pacman.apk
