#!/bin/bash
set -e

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
AAPT_JAR="$ANDROID_JAR"
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
    -I "$AAPT_JAR"

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
    -I "$AAPT_JAR" \
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

echo "=== Patching platformBuildVersionCode to 35 in binary manifest ==="
python3 - << 'PYEOF'
import zipfile, struct, sys

apk_in  = 'bin/pacman-aligned.apk'
apk_out = 'bin/pacman-patched.apk'

with zipfile.ZipFile(apk_in, 'r') as z:
    manifest = bytearray(z.read('AndroidManifest.xml'))

# Parse string pool starting at byte 8 (after 8-byte XML file header)
sp_off       = 8
sp_str_count = struct.unpack_from('<I', manifest, sp_off + 8)[0]
sp_flags     = struct.unpack_from('<I', manifest, sp_off + 16)[0]
sp_str_start = struct.unpack_from('<I', manifest, sp_off + 20)[0]
is_utf8      = bool(sp_flags & 0x100)
offsets = [struct.unpack_from('<I', manifest, sp_off + 28 + i*4)[0]
           for i in range(sp_str_count)]
str_base = sp_off + sp_str_start

def read_str(idx):
    if idx < 0 or idx >= len(offsets):
        return ''
    off = str_base + offsets[idx]
    if is_utf8:
        b0 = manifest[off]; off += 2 if b0 & 0x80 else 1
        b1 = manifest[off]; off += 2 if b1 & 0x80 else 1
        n = (b1 & 0x7f) if b1 & 0x80 else b1
        return manifest[off:off+n].decode('utf-8', 'replace')
    else:
        length = struct.unpack_from('<H', manifest, off)[0]; off += 2
        return manifest[off:off+length*2].decode('utf-16-le', 'replace')

# Find the string index for 'platformBuildVersionCode'
target_idx = next((i for i in range(sp_str_count)
                   if read_str(i) == 'platformBuildVersionCode'), None)
if target_idx is None:
    sys.exit("ERROR: platformBuildVersionCode not found in string pool")

# Each AXML attribute record is 20 bytes:
#   ns(4) name_idx(4) raw_value(4) size(2) res0(1) data_type(1) data(4)
# Scan the entire binary for records where name_idx == target_idx and data_type == 0x10 (int)
patched = 0
pos = 0
while pos <= len(manifest) - 20:
    name_idx  = struct.unpack_from('<i', manifest, pos + 4)[0]
    data_type = manifest[pos + 15]
    if name_idx == target_idx and data_type == 0x10:
        old_val = struct.unpack_from('<I', manifest, pos + 16)[0]
        struct.pack_into('<I', manifest, pos + 16, 35)
        print(f"  patched at 0x{pos:x}: {old_val} -> 35")
        patched += 1
    pos += 1

if patched == 0:
    sys.exit("ERROR: platformBuildVersionCode attribute not found; no patch applied")

with zipfile.ZipFile(apk_in, 'r') as src:
    with zipfile.ZipFile(apk_out, 'w', zipfile.ZIP_DEFLATED) as dst:
        for item in src.infolist():
            if item.filename == 'AndroidManifest.xml':
                info = zipfile.ZipInfo('AndroidManifest.xml')
                info.compress_type = zipfile.ZIP_STORED
                dst.writestr(info, bytes(manifest))
            else:
                dst.writestr(item, src.read(item.filename))
print(f"OK: patched {patched} occurrence(s); wrote {apk_out}")
PYEOF

echo "=== Zipalign patched APK ==="
$ZIPALIGN -f 4 bin/pacman-patched.apk bin/pacman-aligned2.apk

echo "=== Signing APK ==="
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --out bin/pacman.apk \
    bin/pacman-aligned2.apk

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK: $(pwd)/bin/pacman.apk"
ls -lh bin/pacman.apk
