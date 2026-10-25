#!/usr/bin/env bash
# ============================================================
# generate_baseline_profile.sh — Build and package the ART baseline profile
# ============================================================
# Usage:  ./scripts/generate_baseline_profile.sh
#
# Requires a boot emulator or connected device (API >= 28, non-minified
# benchmark variant installable). Check with: adb devices
#
# Everything happens through Gradle; the androidx.baselineprofile plugin
# installs the benchmark APK, drives the UI, collects the profile and copies
# it into the app sources automatically.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

PROFILE_SRC="app/src/release/generated/baselineProfiles/baseline-prof.txt"

if ! adb get-state >/dev/null 2>&1; then
  echo "No device or emulator available."
  echo "Start one with: emulator -avd NexusApi35 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -memory 2048 &"
  echo "then wait for:  adb shell getprop sys.boot_completed"
  exit 1
fi

echo "[1/2] Generating the profile from :macrobenchmark (this drives the app on-device)..."
./gradlew :app:generateBaselineProfile --no-daemon --max-workers=1

echo ""
echo "[2/2] Verifying the profile..."
if [ ! -s "$PROFILE_SRC" ]; then
  echo "FAIL: $PROFILE_SRC is missing or empty."
  exit 1
fi
echo "  rules:      $(grep -c '.' "$PROFILE_SRC")"
echo "  app rules:  $(grep -c '^Lcom/nexus' "$PROFILE_SRC")"

echo ""
echo "Next: build the release APK and confirm the profile is packaged."
echo "  ./gradlew :app:assembleRelease --no-daemon --max-workers=1"
echo "  unzip -l app/build/outputs/apk/release/app-release.apk | grep dexopt"
echo "     assets/dexopt/baseline.prof"
echo "     assets/dexopt/baseline.profm"
