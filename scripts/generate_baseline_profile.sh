#!/usr/bin/env bash
# ============================================================
# generate_baseline_profile.sh — Generate Baseline Profile
# ============================================================
# Usage:  ./scripts/generate_baseline_profile.sh
# Requires a connected device/emulator running the benchmark.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== NEXUS Baseline Profile Generation ==="
echo ""

echo "[1/2] Building benchmark APK..."
cd "$PROJECT_DIR"
./gradlew :macrobenchmark:assembleRelease

echo "[2/2] Running baseline profile generator..."
echo ""
echo "To generate the baseline profile, on a device/emulator run:"
echo ""
echo "  adb install -r macrobenchmark/build/outputs/apk/release/macrobenchmark-release.apk"
echo "  adb install -r app/build/outputs/apk/release/app-release.apk"
echo "  adb shell am start -n com.nexus.app/.BenchmarkActivity"
echo ""
echo "After benchmark completes, pull the profile:"
echo ""
echo "  adb pull /sdcard/Android/media/com.nexus.app/baseline-prof-data/"
echo ""
echo "Copy the generated baseline.prof to:"
echo "  app/src/main/baseline-prof/"
echo ""
echo "Or use the Gradle task (requires root on emulator):"
echo "  ./gradlew :app:generateBaselineProfile"
echo ""
