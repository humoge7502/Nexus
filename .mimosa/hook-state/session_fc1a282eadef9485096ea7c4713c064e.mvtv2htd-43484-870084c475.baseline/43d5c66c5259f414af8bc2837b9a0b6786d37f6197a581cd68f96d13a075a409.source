#!/usr/bin/env bash
# ============================================================
# generate_room_schema.sh — Export Room database schemas
# ============================================================
# Usage:  ./scripts/generate_room_schema.sh
# Requires an Android device/emulator connected.
# Output: app/build/generated/source/room/schema/
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Generating Room Schema ==="
echo "This requires an Android SDK build. Run:"
echo ""
echo "  cd $PROJECT_DIR"
echo "  ./gradlew :core:database:kaptDebugKotlin  # if using kapt"
echo "  # OR"
echo "  ./gradlew :core:database:kspDebugKotlin   # if using KSP"
echo ""
echo "Schema files will be at:"
echo "  core/database/build/generated/source/room/schema/"
echo ""
echo "Export schema directory is configured in core/database/build.gradle.kts:"
echo "  room { schemaLocation(\"\$projectDir/schemas\") }"
echo ""

# Verify the schema directory exists
SCHEMA_DIR="${PROJECT_DIR}/core/database/schemas"
if [ -d "$SCHEMA_DIR" ]; then
  echo "Current schemas:"
  ls -la "$SCHEMA_DIR"/
else
  echo "No schemas directory yet. Run ./gradlew :core:database:kspDebugKotlin first."
fi
