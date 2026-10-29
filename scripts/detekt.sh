#!/usr/bin/env bash
# Runs detekt over every Kotlin module without adding a Gradle plugin, so the
# offline build stays untouched. CI downloads the CLI jar once and caches it.
#
#   ./scripts/detekt.sh                     # check (uses the baseline if present)
#   DETEKT_CREATE_BASELINE=1 ./scripts/detekt.sh   # regenerate the baseline
set -euo pipefail

VERSION="${DETEKT_VERSION:-1.23.8}"
JAR="detekt-cli-${VERSION}-all.jar"
MAX_ISSUES="${DETEKT_MAX_ISSUES:-0}"
BASELINE="config/detekt/baseline.xml"

if [ ! -f "$JAR" ]; then
  echo "Downloading detekt ${VERSION}..."
  curl -sSLo "$JAR" \
    "https://github.com/detekt/detekt/releases/download/v${VERSION}/${JAR}"
fi

mkdir -p build/reports/detekt

COMMON_ARGS=(
  --config config/detekt/detekt.yml
  --build-upon-default-config
  --input core,feature,app,audit-core,audit-scanner,audit-rules,audit-reporting,phase-analyzers,audit-cli,macrobenchmark
  --excludes '**/build/**,**/generated/**,**/resources/**'
  --parallel
)

if [ "${DETEKT_CREATE_BASELINE:-0}" = "1" ]; then
  echo "Creating detekt baseline at ${BASELINE}..."
  java -jar "$JAR" "${COMMON_ARGS[@]}" --create-baseline --baseline "$BASELINE" || true
  echo "Baseline written. Review it before committing."
  exit 0
fi

BASELINE_ARGS=()
if [ -f "$BASELINE" ]; then
  BASELINE_ARGS=(--baseline "$BASELINE")
fi

java -jar "$JAR" \
  "${COMMON_ARGS[@]}" \
  "${BASELINE_ARGS[@]}" \
  --max-issues "$MAX_ISSUES" \
  --report "xml:build/reports/detekt/detekt.xml" \
  --report "html:build/reports/detekt/detekt.html"
