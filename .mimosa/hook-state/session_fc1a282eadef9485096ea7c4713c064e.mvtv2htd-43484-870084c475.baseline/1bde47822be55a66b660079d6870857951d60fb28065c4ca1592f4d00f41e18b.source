#!/usr/bin/env bash
# ============================================================
# verify_release_build.sh — Run full release build verification
# ============================================================
# Usage:  ./scripts/verify_release_build.sh
# This runs assembleRelease and validates the output APK.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APK_PATH="${PROJECT_DIR}/app/build/outputs/apk/release/app-release.apk"

echo "=== NEXUS Release Build Verification ==="
echo ""

# Step 1: Clean
echo "[1/5] Cleaning previous builds..."
cd "$PROJECT_DIR"
./gradlew clean --quiet

# Step 2: Assemble release
echo "[2/5] Building release APK..."
./gradlew :app:assembleRelease

# Step 3: Verify APK exists
echo "[3/5] Verifying APK..."
if [ ! -f "$APK_PATH" ]; then
  echo "ERROR: APK not found at $APK_PATH"
  exit 1
fi
echo "APK size: $(du -h "$APK_PATH" | cut -f1)"

# Step 4: Check minification
echo "[4/5] Checking ProGuard mapping..."
MAPPING="${PROJECT_DIR}/app/build/outputs/mapping/release/mapping.txt"
if [ -f "$MAPPING" ]; then
  echo "ProGuard mapping found: $(wc -l < "$MAPPING") lines"
else
  echo "WARNING: No ProGuard mapping found. obfuscation may be disabled."
fi

# Step 5: Verify APK signature
echo "[5/5] Verifying APK signature..."
if command -v apksigner &> /dev/null; then
  apksigner verify --print-certs "$APK_PATH"
elif command -v jarsigner &> /dev/null; then
  jarsigner -verify -verbose -certs "$APK_PATH" 2>&1 | head -20
else
  echo "Neither apksigner nor jarsigner found. Install Android SDK build-tools."
fi

echo ""
echo "=== Build Verification Complete ==="
echo "APK: $APK_PATH"
